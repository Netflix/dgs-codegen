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
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Files
import java.nio.file.Path

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
    fun `duplicate destinations fail before writing`() {
        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles = listOf(javaFile("Duplicate"), javaFile("Duplicate")),
                kotlinFiles = emptyList(),
                outputDirectory = temporaryDirectory,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("example/Duplicate.java")

        assertThat(Files.exists(temporaryDirectory.resolve("example/Duplicate.java"))).isFalse()
    }

    @Test
    fun `case-only duplicate destinations fail before writing`() {
        assertThatThrownBy {
            GeneratedFileWriter(4).write(
                javaFiles = listOf(javaFile("Duplicate"), javaFile("duplicate")),
                kotlinFiles = emptyList(),
                outputDirectory = temporaryDirectory,
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("example/Duplicate.java")
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
    ): JavaFile = JavaFile.builder(packageName, TypeSpec.classBuilder(name).build()).build()

    private fun largeJavaFile(name: String): JavaFile {
        val type = TypeSpec.classBuilder(name)
        repeat(50_000) { field ->
            type.addField(FieldSpec.builder(TypeName.INT, "field$field").build())
        }
        return JavaFile.builder("large", type.build()).build()
    }

    private fun writerThreads(): List<Thread> =
        Thread.getAllStackTraces().keys.filter { it.isAlive && it.name.startsWith("dgs-codegen-writer-") }

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
