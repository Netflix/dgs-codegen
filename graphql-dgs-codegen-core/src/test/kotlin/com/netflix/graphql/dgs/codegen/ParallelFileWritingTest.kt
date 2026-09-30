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

import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.TypeName
import com.palantir.javapoet.TypeSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ParallelFileWritingTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @ParameterizedTest
    @EnumSource(Language::class)
    fun `parallel writes emit the same complete file map as serial writes`(language: Language) {
        val serialOutput = generate(language, "serial", 1)
        val expected = readFiles(serialOutput)

        val parallelOutput = generate(language, "parallel", 4)
        assertThat(readFiles(parallelOutput)).isEqualTo(expected)
    }

    @Test
    fun `write failures identify the generated source`() {
        val output = temporaryDirectory.resolve("failure-output")
        Files.createDirectories(output)
        Files.writeString(output.resolve("broken"), "occupied")

        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles =
                    listOf(
                        javaFile("Failure", "broken"),
                        largeJavaFile("LargeOne"),
                        largeJavaFile("LargeTwo"),
                        largeJavaFile("LargeThree"),
                    ),
                kotlinFiles = emptyList(),
                outputDirectory = output,
            )
        }.isInstanceOf(CodeGenFileWriteException::class.java)
            .hasMessageContaining("broken/Failure.java")

        assertThat(writerThreads()).isEmpty()
    }

    @Test
    fun `Kotlin render failures retain their original exception`() {
        val failingFile =
            FileSpec
                .builder("example", "Broken")
                .addProperty(
                    PropertySpec
                        .builder("value", ClassName("java.lang", "String[]"))
                        .initializer("TODO()")
                        .build(),
                ).build()
        val anotherFile =
            FileSpec
                .builder("example", "Another")
                .addProperty(PropertySpec.builder("value", Int::class).initializer("0").build())
                .build()

        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles = emptyList(),
                kotlinFiles = listOf(failingFile, anotherFile),
                outputDirectory = temporaryDirectory,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Can't escape identifier")
            .hasMessageContaining("String[]")
            .satisfies({ failure ->
                assertThat(failure.suppressed.filterIsInstance<GeneratedFileDestination>().map { it.destination })
                    .containsExactly(temporaryDirectory.resolve("example/Broken.kt"))
            })
    }

    @Test
    fun `pre-interrupted caller writes serially and keeps its interrupt flag`() {
        val currentThread = Thread.currentThread()
        currentThread.interrupt()

        try {
            GeneratedFileWriter(4) { _, _ ->
                throw AssertionError("A pre-interrupted caller should not create a writer executor")
            }.write(
                javaFiles = listOf(javaFile("First"), javaFile("Second")),
                kotlinFiles = emptyList(),
                outputDirectory = temporaryDirectory,
            )

            assertThat(currentThread.isInterrupted).isTrue()
            assertThat(Files.exists(temporaryDirectory.resolve("example/First.java"))).isTrue()
            assertThat(Files.exists(temporaryDirectory.resolve("example/Second.java"))).isTrue()
        } finally {
            Thread.interrupted()
        }
    }

    @Test
    fun `rejected submissions shut down the writer executor`() {
        val executor = RejectAfterFirstSubmission(Executors.newFixedThreadPool(4))

        assertThatThrownBy {
            GeneratedFileWriter(4) { _, _ -> executor }.write(
                javaFiles = listOf(javaFile("First"), javaFile("Second")),
                kotlinFiles = emptyList(),
                outputDirectory = temporaryDirectory,
            )
        }.isInstanceOf(RejectedExecutionException::class.java)

        assertThat(executor.isShutdown).isTrue()
        assertThat(executor.isTerminated).isTrue()
    }

    @Test
    fun `exact duplicate destinations keep the last source`() {
        GeneratedFileWriter(4).write(
            javaFiles = listOf(javaFile("Duplicate", field = "first"), javaFile("Duplicate", field = "last")),
            kotlinFiles = emptyList(),
            outputDirectory = temporaryDirectory,
        )

        assertThat(Files.readString(temporaryDirectory.resolve("example/Duplicate.java")))
            .contains("last")
            .doesNotContain("first")
    }

    @Test
    fun `exact duplicate Kotlin destinations keep the last source`() {
        val first =
            FileSpec
                .builder(
                    "example",
                    "Duplicate",
                ).addProperty(PropertySpec.builder("first", Int::class).initializer("1").build())
                .build()
        val last =
            FileSpec
                .builder(
                    "example",
                    "Duplicate",
                ).addProperty(PropertySpec.builder("last", Int::class).initializer("2").build())
                .build()

        GeneratedFileWriter(4).write(
            javaFiles = emptyList(),
            kotlinFiles = listOf(first, last),
            outputDirectory = temporaryDirectory,
        )

        assertThat(Files.readString(temporaryDirectory.resolve("example/Duplicate.kt")))
            .contains("last")
            .doesNotContain("first")
    }

    @Test
    fun `case-only duplicate destinations follow the output filesystem`() {
        val output = temporaryDirectory.resolve("case-collision-output")
        val caseSensitive = isCaseSensitiveFileSystem(output)

        if (caseSensitive) {
            GeneratedFileWriter(4).write(
                javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                kotlinFiles = emptyList(),
                outputDirectory = output,
            )

            val generatedNames =
                Files.list(output.resolve("example")).use { paths ->
                    paths.map { it.fileName.toString() }.toList()
                }
            assertThat(generatedNames).containsExactlyInAnyOrder("Duplicate.java", "duplicate.java")
        } else {
            assertThatThrownBy {
                GeneratedFileWriter(4).write(
                    javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                    kotlinFiles = emptyList(),
                    outputDirectory = output,
                )
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Multiple generated sources target the same path: example/Duplicate.java")
        }

        if (Files.exists(output)) {
            assertThat(Files.list(output).use { paths -> paths.filter(Files::isRegularFile).count() }).isZero()
        }
    }

    @Test
    fun `case-only duplicates are rejected when the probe reports a case-insensitive directory`() {
        val output = temporaryDirectory.resolve("injected-insensitive")

        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                kotlinFiles = emptyList(),
                outputDirectory = output,
                isCaseInsensitiveDirectory = { true },
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Multiple generated sources target the same path: example/Duplicate.java")

        assertThat(output).doesNotExist()
    }

    @Test
    fun `case-only duplicates are written when the probe reports a case-sensitive directory`() {
        val output = temporaryDirectory.resolve("injected-sensitive")

        GeneratedFileWriter(4).write(
            javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
            kotlinFiles = emptyList(),
            outputDirectory = output,
            isCaseInsensitiveDirectory = { false },
        )

        assertThat(Files.exists(output.resolve("example/Duplicate.java"))).isTrue()
        assertThat(Files.exists(output.resolve("example/duplicate.java"))).isTrue()
    }

    @Test
    fun `the probe is not consulted without a case collision`() {
        val probed = mutableListOf<Path>()

        GeneratedFileWriter(4).write(
            javaFiles = listOf(javaFile("First"), javaFile("Second")),
            kotlinFiles = emptyList(),
            outputDirectory = temporaryDirectory.resolve("injected-no-collision"),
            isCaseInsensitiveDirectory = { directory -> probed.add(directory).let { true } },
        )

        assertThat(probed).isEmpty()
    }

    @Test
    fun `each destination directory is probed once and a nested insensitive directory is rejected`() {
        val output = temporaryDirectory.resolve("injected-nested")
        val nested = output.resolve("nested").normalize()
        val probed = mutableListOf<Path>()
        val writer = GeneratedFileWriter(4)
        val probe = { directory: Path ->
            probed.add(directory)
            directory == nested
        }

        // Case-sensitive root: the collision in the root directory is written.
        writer.write(
            javaFiles = listOf(javaFile("Duplicate", packageName = ""), javaFile("duplicate", packageName = "")),
            kotlinFiles = emptyList(),
            outputDirectory = output,
            isCaseInsensitiveDirectory = probe,
        )
        assertThat(probed).containsExactly(output.normalize())

        probed.clear()
        assertThatThrownBy {
            writer.write(
                javaFiles =
                    listOf(
                        javaFile("Duplicate", packageName = ""),
                        javaFile("duplicate", packageName = ""),
                        javaFile("Nested", packageName = "nested"),
                        javaFile("nested", packageName = "nested"),
                    ),
                kotlinFiles = emptyList(),
                outputDirectory = output,
                isCaseInsensitiveDirectory = probe,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Multiple generated sources target the same path: nested/Nested.java")
        assertThat(probed).containsExactlyInAnyOrder(output.normalize(), nested)
    }

    @Test
    fun `case-sensitive parent keeps case-variant directories distinct`() {
        val output = temporaryDirectory.resolve("case-variant-directories")
        val probed = mutableListOf<Path>()

        GeneratedFileWriter(1).write(
            javaFiles = listOf(javaFile("A", packageName = "Foo"), javaFile("a", packageName = "foo")),
            kotlinFiles = emptyList(),
            outputDirectory = output,
            isCaseInsensitiveDirectory = { directory ->
                probed.add(directory)
                directory != output
            },
        )

        assertThat(probed).containsExactly(output)
    }

    @Test
    fun `probe failures are reported as write failures`() {
        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                kotlinFiles = emptyList(),
                outputDirectory = temporaryDirectory.resolve("injected-failure"),
                isCaseInsensitiveDirectory = { throw AccessDeniedException("probe") },
            )
        }.isInstanceOf(CodeGenFileWriteException::class.java)
            .hasCauseInstanceOf(AccessDeniedException::class.java)
    }

    @Test
    fun `the real probe leaves no directories behind when it rejects a collision`() {
        val root = temporaryDirectory.resolve("real-probe")
        Files.createDirectories(root)
        val output = root.resolve("deep/nested")

        val failure =
            runCatching {
                GeneratedFileWriter(4).write(
                    javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                    kotlinFiles = emptyList(),
                    outputDirectory = output,
                )
            }.exceptionOrNull()

        // Only a case-insensitive host rejects the pair; a case-sensitive host writes both files.
        if (failure != null) {
            assertThat(failure).isInstanceOf(IllegalArgumentException::class.java)
            assertThat(root.resolve("deep")).doesNotExist()
        }
        Files.list(root).use { paths ->
            assertThat(paths.filter { it.fileName.toString().startsWith(".dgs-case-probe-") }.count()).isZero()
        }
    }

    private fun generate(
        language: Language,
        directoryName: String,
        fileWriteParallelism: Int,
    ): Path {
        val output = temporaryDirectory.resolve(directoryName)
        CodeGen(
            CodeGenConfig(
                schemas = setOf(SCHEMA),
                outputDir = output,
                examplesOutputDir = temporaryDirectory.resolve("$directoryName-examples"),
                writeToFiles = true,
                packageName = "com.netflix.generated",
                language = language,
                generateClientApi = true,
                generateInterfaces = true,
            ).apply { this.fileWriteParallelism = fileWriteParallelism },
        ).generate()
        return output
    }

    private fun readFiles(directory: Path): Map<String, String> =
        Files.walk(directory).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .sorted()
                .iterator()
                .asSequence()
                .associate { path -> directory.relativize(path).toString() to Files.readString(path) }
        }

    private fun javaFile(
        name: String,
        packageName: String = "example",
        field: String? = null,
    ): JavaFile {
        val type = TypeSpec.classBuilder(name)
        if (field != null) type.addField(FieldSpec.builder(TypeName.INT, field).build())
        return JavaFile.builder(packageName, type.build()).build()
    }

    private fun largeJavaFile(name: String): JavaFile {
        val type = TypeSpec.classBuilder(name)
        repeat(50_000) { field ->
            type.addField(FieldSpec.builder(TypeName.INT, "field$field").build())
        }
        return JavaFile.builder("large", type.build()).build()
    }

    private fun writerThreads(): List<Thread> =
        Thread.getAllStackTraces().keys.filter { it.isAlive && it.name.startsWith("dgs-codegen-writer-") }

    private class RejectAfterFirstSubmission(
        private val delegate: ExecutorService,
    ) : AbstractExecutorService() {
        private val submissions = AtomicInteger()

        override fun execute(command: Runnable) {
            if (submissions.incrementAndGet() == 2) {
                throw RejectedExecutionException("Rejected second writer task")
            }
            delegate.execute(command)
        }

        override fun shutdown() = delegate.shutdown()

        override fun shutdownNow(): List<Runnable> = delegate.shutdownNow()

        override fun isShutdown(): Boolean = delegate.isShutdown

        override fun isTerminated(): Boolean = delegate.isTerminated

        override fun awaitTermination(
            timeout: Long,
            unit: TimeUnit,
        ): Boolean = delegate.awaitTermination(timeout, unit)
    }

    private fun isCaseSensitiveFileSystem(directory: Path): Boolean {
        Files.createDirectories(directory)
        val probe = Files.createTempFile(directory, ".case-probe-", ".tmp")
        try {
            val uppercaseProbe = probe.resolveSibling(probe.fileName.toString().uppercase(Locale.ROOT))
            return try {
                !Files.isSameFile(probe, uppercaseProbe)
            } catch (_: NoSuchFileException) {
                true
            }
        } finally {
            Files.deleteIfExists(probe)
        }
    }

    companion object {
        private val SCHEMA =
            """
            interface Character {
                name: String!
            }

            type Hero implements Character {
                name: String!
                power: Power!
            }

            enum Power {
                FLIGHT
                SPEED
            }

            input HeroInput {
                name: String!
                power: Power!
            }

            type Query {
                heroes: [Hero!]!
            }

            type Mutation {
                addHero(input: HeroInput!): Hero!
            }
            """.trimIndent()
    }
}
