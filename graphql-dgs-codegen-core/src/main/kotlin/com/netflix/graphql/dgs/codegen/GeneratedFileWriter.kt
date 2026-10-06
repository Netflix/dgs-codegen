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
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class GeneratedFileWriter(
    private val parallelism: Int,
    private val executorFactory: (Int, ThreadFactory) -> ExecutorService = { poolSize, threadFactory ->
        Executors.newFixedThreadPool(poolSize, threadFactory)
    },
) {
    init {
        require(parallelism > 0) { "File write parallelism must be greater than zero" }
    }

    fun write(
        javaFiles: List<JavaFile>,
        kotlinFiles: List<FileSpec>,
        outputDirectory: Path,
        isCaseInsensitiveDirectory: (Path) -> Boolean = this::probeCaseInsensitivity,
    ) {
        // Sequential writes through 8.7.0 left the last source at an exact destination.
        val files =
            (
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
            ).associateBy { it.relativePath.normalize() }
                .values
                .toList()

        validateUniqueDestinations(files, outputDirectory, isCaseInsensitiveDirectory)
        if (parallelism == 1 || files.size < 2) {
            files.forEach { write(it, outputDirectory) }
            return
        }

        if (Thread.interrupted()) {
            try {
                files.forEach { write(it, outputDirectory) }
            } finally {
                Thread.currentThread().interrupt()
            }
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
            executorFactory(minOf(parallelism, files.size)) { runnable ->
                Thread(runnable, "dgs-codegen-writer-${threadNumber.incrementAndGet()}")
            }
        val completionService = ExecutorCompletionService<Unit>(executor)
        val futures = mutableListOf<Future<Unit>>()

        try {
            files.forEach { file ->
                futures += completionService.submit(Callable { write(file, outputDirectory) })
            }
            repeat(files.size) {
                completionService.take().get()
            }
            executor.shutdown()
        } catch (exception: Throwable) {
            futures.forEach { it.cancel(true) }
            executor.shutdownNow()
            awaitTermination(executor)
            when (exception) {
                is InterruptedException -> {
                    Thread.currentThread().interrupt()
                    throw CodeGenFileWriteException.interrupted(outputDirectory, exception)
                }

                is ExecutionException -> {
                    throw exception.cause ?: exception
                }

                else -> {
                    throw exception
                }
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
        } catch (exception: IOException) {
            throw CodeGenFileWriteException(destination, exception)
        } catch (exception: Exception) {
            // Keep the original type, as 8.7.0 did, and make the destination reachable.
            exception.addSuppressed(GeneratedFileDestination(destination))
            throw exception
        }
    }

    private fun validateUniqueDestinations(
        files: List<GeneratedFile>,
        outputDirectory: Path,
        isCaseInsensitiveDirectory: (Path) -> Boolean,
    ) {
        // A case-sensitive parent keeps its children distinct even when a child directory is case-insensitive.
        val insensitiveByDirectory = mutableMapOf<Path, Boolean>()
        files
            .groupBy {
                it.relativePath
                    .normalize()
                    .toString()
                    .lowercase(Locale.ROOT)
            }.values
            .filter { it.size > 1 }
            .forEach { collision ->
                val paths = collision.map { it.relativePath.normalize() }
                val duplicate =
                    paths.indices.any { first ->
                        (first + 1 until paths.size).any { second ->
                            destinationsCollide(
                                paths[first],
                                paths[second],
                                outputDirectory,
                                isCaseInsensitiveDirectory,
                                insensitiveByDirectory,
                            )
                        }
                    }
                if (duplicate) {
                    throw IllegalArgumentException(
                        "Multiple generated sources target the same path: ${collision.first().relativePath}",
                    )
                }
            }
    }

    private fun destinationsCollide(
        first: Path,
        second: Path,
        outputDirectory: Path,
        isCaseInsensitiveDirectory: (Path) -> Boolean,
        insensitiveByDirectory: MutableMap<Path, Boolean>,
    ): Boolean {
        var directory = outputDirectory.normalize()
        for (index in 0 until first.nameCount) {
            val component = first.getName(index)
            if (component != second.getName(index)) {
                val insensitive =
                    insensitiveByDirectory.getOrPut(directory) {
                        try {
                            isCaseInsensitiveDirectory(directory)
                        } catch (exception: IOException) {
                            throw CodeGenFileWriteException(directory, exception)
                        }
                    }
                if (!insensitive) return false
            }
            directory = directory.resolve(component)
        }
        return true
    }

    /**
     * Probes the nearest existing ancestor of [directory], so the probe creates no directories and leaves none behind.
     * The probe file is deleted before returning.
     */
    private fun probeCaseInsensitivity(directory: Path): Boolean {
        var existing = directory.toAbsolutePath()
        while (!Files.isDirectory(existing)) {
            existing = existing.parent ?: break
        }
        val probe = Files.createTempFile(existing, ".dgs-case-probe-", ".tmp")
        try {
            val uppercaseProbe = probe.resolveSibling(probe.fileName.toString().uppercase(Locale.ROOT))
            return try {
                Files.isSameFile(probe, uppercaseProbe)
            } catch (_: NoSuchFileException) {
                false
            }
        } finally {
            Files.deleteIfExists(probe)
        }
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
                    // shutdownNow() requests interruption, it does not guarantee a blocked writer exits.
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

    private data class GeneratedFile(
        val relativePath: Path,
        val write: () -> Unit,
    )
}

/** Suppressed onto a non-IO write failure so the original exception type is kept and the destination stays reachable. */
internal class GeneratedFileDestination(
    val destination: Path,
) : RuntimeException("Failed while writing '$destination'", null, false, false)

class CodeGenFileWriteException internal constructor(
    val generatedFile: Path,
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause) {
    constructor(generatedFile: Path, cause: Throwable) :
        this(generatedFile, "Failed to write generated source '$generatedFile'", cause)

    internal companion object {
        fun interrupted(
            outputDirectory: Path,
            cause: InterruptedException,
        ) = CodeGenFileWriteException(
            outputDirectory,
            "Writing generated files was interrupted, files in '$outputDirectory' may be incomplete",
            cause,
        )
    }
}
