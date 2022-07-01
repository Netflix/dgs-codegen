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

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.streams.toList

class Kotlin2CodeGenTest {

    // set this to true to update all expected outputs instead of running tests
    private val updateExpected = false

    @ParameterizedTest
    @MethodSource("listTestsToRun")
    fun testCodeGen(testName: String) {
        val schema = readResource("/kotlin2/$testName/schema.graphql")

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = "kotlin2.$testName.expected",
                language = Language.KOTLIN,
                generateClientApi = true,
                generateKotlinNullableClasses = true,
                generateKotlinClosureProjections = true,
                typeMapping = when (testName) {
                    "dataClassWithMappedTypes" -> mapOf(
                        "Long" to "kotlin.Long",
                        "DateTime" to "java.time.OffsetDateTime",
                        "PageInfo" to "graphql.relay.PageInfo",
                    )
                    else -> emptyMap()
                }
            )
        ).generate()

        val fileNames = codeGenResult.kotlinSources()
            .groupingBy { it.packageName.substringAfterLast('.') to it.name }
            .eachCount()

        // fail if any file was defined twice
        fileNames
            .filterValues { it > 1 }
            .keys
            .forEach { fail("Duplicate file: ${it.first}.${it.second}") }

        // fail if any file was expected that's not generated
        listAllFiles("/kotlin2/$testName/expected")
            .map {
                it.getName(it.nameCount - 2).toString() to it.getName(it.nameCount - 1).toString().removeSuffix(".kt")
            }
            .toSet().subtract(fileNames.keys)
            .forEach { fail("Missing expected file: ${it.first}.${it.second}") }

        codeGenResult.kotlinSources().forEach { spec ->

            val type = spec.packageName.substringAfterLast("expected").trimStart('.')
            val fileName = "/kotlin2/$testName/expected/$type/${spec.name}.kt"
            val actual = spec.toString()

            if (updateExpected) {
                writeExpected(fileName, actual)
            } else {
                assertThat(actual).isEqualTo(readResource(fileName))
            }
        }

        assertCompilesKotlin(codeGenResult)
    }

    companion object {

        @Suppress("unused")
        @JvmStatic
        fun listTestsToRun(): List<String> {
            return getAbsolutePath("kotlin2")
                .listDirectoryEntries()
                .map { it.getName(it.nameCount.dec()).toString() }
                .sorted()
        }

        private fun getAbsolutePath(suffix: String): Path {
            val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
            return Paths.get(projectDirAbsolutePath, "/src/test/resources/$suffix")
        }

        private fun listAllFiles(suffix: String): List<Path> {
            val path = getAbsolutePath(suffix)
            if (!path.exists()) return emptyList()
            return Files.walk(path)
                .filter { Files.isRegularFile(it) }
                .toList()
        }

        private fun readResource(fileName: String): String {
            return this::class.java.getResource(fileName)
                ?.readText()
                ?: throw IllegalArgumentException("Missing file: $fileName")
        }

        private fun writeExpected(fileName: String, content: String) {
            val path = getAbsolutePath(fileName)

            if (!path.exists()) {
                path.parent.createDirectories()
            }

            path.toFile().writeText(content)
        }
    }
}
