/*
 * Copyright 2026 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.codegen

import com.sun.management.ThreadMXBean
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.lang.management.ManagementFactory
import java.nio.file.Path
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class DependencySchemaLoadingTest {
    @Test
    fun `Dependency schemas do not allocate an extra schema copy`(
        @TempDir directory: Path,
    ) {
        val schema = directory.resolve("schema.graphqls").toFile()
        schema.outputStream().use { output ->
            output.write("# ".toByteArray())
            val chunk = ByteArray(8192) { 'a'.code.toByte() }
            repeat(2048) { output.write(chunk) }
            output.write("\ntype Query { value: String }\n".toByteArray())
        }
        val jar = directory.resolve("schema.jar").toFile()
        JarOutputStream(jar.outputStream()).use { output ->
            output.putNextEntry(ZipEntry("schema.graphqls"))
            schema.inputStream().use { it.copyTo(output) }
            output.closeEntry()
        }

        val warmupFile = directory.resolve("warmup.graphqls").toFile()
        warmupFile.writeText("type Query { value: String }")
        allocatedBytes(CodeGenConfig(schemaFiles = setOf(warmupFile), packageName = "com.example"))
        allocatedBytes(
            CodeGenConfig(schemaJarFilesFromDependencies = listOf(schemaJar(directory, "warmup.jar")), packageName = "com.example"),
        )

        val fileAllocations = allocatedBytes(CodeGenConfig(schemaFiles = setOf(schema), packageName = "com.example"))
        val jarAllocations = allocatedBytes(CodeGenConfig(schemaJarFilesFromDependencies = listOf(jar), packageName = "com.example"))

        // Compare allocation for identical input, so GC timing and the test worker's heap size cannot hide an extra copy.
        assertThat(jarAllocations - fileAllocations)
            .describedAs("Extra bytes allocated for a dependency schema instead of the same file schema")
            .isLessThan(schema.length() / 2)
    }

    @Test
    fun `Schema discovery failure closes earlier dependency jars`(
        @TempDir directory: Path,
    ) {
        val first = schemaJar(directory, "first.jar")
        val second = directory.resolve("second.jar").toFile()
        val failure = IOException("Cannot open the second dependency")
        val closed = mutableListOf<File>()

        assertThatThrownBy {
            CodeGen.withSchemaSources(
                CodeGenConfig(schemaJarFilesFromDependencies = listOf(first, second)),
                File::reader,
                { file ->
                    if (file == second) throw failure
                    object : JarFile(file) {
                        override fun close() {
                            super.close()
                            closed += file
                        }
                    }
                },
            )
        }.isSameAs(failure)

        assertThat(closed).containsExactly(first)
    }

    @Test
    fun `Schema entry open failure closes every dependency jar`(
        @TempDir directory: Path,
    ) {
        val first = schemaJar(directory, "first.jar")
        val second = schemaJar(directory, "second.jar")
        val failure = IOException("Cannot open the second schema")
        val closed = mutableListOf<File>()

        assertThatThrownBy {
            CodeGen.withSchemaSources(
                CodeGenConfig(schemaJarFilesFromDependencies = listOf(first, second)),
                File::reader,
                { file ->
                    object : JarFile(file) {
                        override fun getInputStream(entry: ZipEntry): InputStream {
                            if (file == second) throw failure
                            return super.getInputStream(entry)
                        }

                        override fun close() {
                            super.close()
                            closed += file
                        }
                    }
                },
            )
        }.isSameAs(failure)

        assertThat(closed).containsExactlyInAnyOrder(first, second)
    }

    private fun schemaJar(
        directory: Path,
        name: String,
    ): File =
        directory.resolve(name).toFile().also { file ->
            JarOutputStream(file.outputStream()).use { output ->
                output.putNextEntry(ZipEntry("schema.graphqls"))
                output.write("type Query { value: String }".toByteArray())
                output.closeEntry()
            }
        }

    private fun allocatedBytes(config: CodeGenConfig): Long {
        val threads = ManagementFactory.getThreadMXBean() as ThreadMXBean
        threads.isThreadAllocatedMemoryEnabled = true
        val threadId = Thread.currentThread().id
        val before = threads.getThreadAllocatedBytes(threadId)
        val result = CodeGen(config).generate()
        check(
            result.javaDataFetchers
                .single()
                .typeSpec()
                .name() == "ValueDatafetcher",
        )
        return threads.getThreadAllocatedBytes(threadId) - before
    }
}
