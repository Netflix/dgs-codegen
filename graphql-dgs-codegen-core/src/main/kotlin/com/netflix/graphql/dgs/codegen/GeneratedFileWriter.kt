/*
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netflix.graphql.dgs.codegen

import com.palantir.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class GeneratedFileWriter(
    private val parallelism: Int,
) {
    init {
        require(parallelism > 0) { "File write parallelism must be greater than zero" }
    }

    fun write(
        javaFiles: List<JavaFile>,
        kotlinFiles: List<FileSpec>,
        outputDirectory: Path,
    ) {
        val files =
            javaFiles.map { javaFile ->
                val relativePath =
                    Path
                        .of(javaFile.packageName().replace('.', '/'))
                        .resolve("${javaFile.typeSpec().name()}.java")
                GeneratedFile(relativePath) { javaFile.writeTo(outputDirectory) }
            } +
                kotlinFiles.map { kotlinFile ->
                    val relativePath = Path.of(kotlinFile.relativePath)
                    GeneratedFile(relativePath) { kotlinFile.writeTo(outputDirectory) }
                }

        validateUniqueDestinations(files)
        if (parallelism == 1 || files.size < 2) {
            files.forEach { write(it, outputDirectory) }
            return
        }

        writeInParallel(files, outputDirectory)
    }

    private fun writeInParallel(
        files: List<GeneratedFile>,
        outputDirectory: Path,
    ) {
        val threadNumber = AtomicInteger()
        val executor =
            Executors.newFixedThreadPool(minOf(parallelism, files.size)) { runnable ->
                Thread(runnable, "dgs-codegen-writer-${threadNumber.incrementAndGet()}")
            }
        val completionService = ExecutorCompletionService<Unit>(executor)
        val futures =
            files.map { file ->
                completionService.submit(Callable { write(file, outputDirectory) })
            }

        try {
            repeat(files.size) {
                completionService.take().get()
            }
            executor.shutdown()
        } catch (exception: InterruptedException) {
            cancel(futures)
            executor.shutdownNow()
            awaitTermination(executor)
            Thread.currentThread().interrupt()
            throw CodeGenFileWriteException(outputDirectory, exception)
        } catch (exception: ExecutionException) {
            cancel(futures)
            executor.shutdownNow()
            awaitTermination(executor)
            throw exception.cause ?: exception
        } finally {
            if (!executor.isShutdown) {
                executor.shutdownNow()
            }
        }
    }

    private fun write(
        file: GeneratedFile,
        outputDirectory: Path,
    ) {
        val destination = outputDirectory.resolve(file.relativePath)
        try {
            Files.createDirectories(destination.parent)
            file.write()
        } catch (exception: Exception) {
            throw CodeGenFileWriteException(destination, exception)
        }
    }

    private fun validateUniqueDestinations(files: List<GeneratedFile>) {
        val duplicate =
            files
                .groupBy {
                    it.relativePath
                        .normalize()
                        .toString()
                        .lowercase(Locale.ROOT)
                }.entries
                .firstOrNull { it.value.size > 1 }
                ?.value
                ?.first()
                ?.relativePath
        require(duplicate == null) { "Multiple generated sources target the same path: $duplicate" }
    }

    private fun awaitTermination(executor: ExecutorService) {
        var interrupted = false
        val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(1)
        try {
            while (!executor.isTerminated) {
                val remaining = deadline - System.nanoTime()
                if (remaining <= 0) {
                    return
                }
                try {
                    executor.awaitTermination(remaining, TimeUnit.NANOSECONDS)
                } catch (_: InterruptedException) {
                    interrupted = true
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun cancel(futures: List<Future<Unit>>) {
        futures.forEach { it.cancel(true) }
    }

    private data class GeneratedFile(
        val relativePath: Path,
        val write: () -> Unit,
    )
}

class CodeGenFileWriteException(
    val generatedFile: Path,
    cause: Throwable,
) : RuntimeException("Failed to write generated source '$generatedFile'", cause)
