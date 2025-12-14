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
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class JavaCodeGenTest {
    // set this to true to update all expected outputs instead of running tests
    private val updateExpected = false

    @ParameterizedTest
    @MethodSource("constantsTestCases")
    fun testCodeGen_constants(testCaseName: String) {
        val testPackage = "constants"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("dataClassesTestCases")
    fun testCodeGen_dataClasses(testCaseName: String) {
        val testPackage = "dataclasses"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        codeGenConfig.typeMapping =
            when (testCaseName) {
                "dataClassWithMappedTypes" ->
                    mapOf(
                        "Long" to "java.lang.Long",
                        "DateTime" to "java.time.OffsetDateTime",
                        "PageInfo" to "graphql.relay.PageInfo",
                        "EntityConnection" to
                            "graphql.relay.SimpleListConnection<com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.types.EntityEdge>",
                    )
                "dataClassWithMappedInterfaces" ->
                    mapOf(
                        "Node" to "com.netflix.graphql.dgs.codegen.java.fixtures.Node",
                    )
                else -> emptyMap()
            }

        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("enumsTestCases")
    fun testCodeGen_enums(testCaseName: String) {
        val testPackage = "enums"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("inputsTestCases")
    fun testCodeGen_inputs(testCaseName: String) {
        val testPackage = "inputs"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        codeGenConfig.typeMapping =
            when (testCaseName) {
                "inputWithDefaultBigDecimal" ->
                    mapOf(
                        "Decimal" to "java.math.BigDecimal",
                    )
                "inputWithDefaultCurrency" ->
                    mapOf(
                        "Currency" to "java.util.Currency",
                    )
                else -> emptyMap()
            }

        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("interfacesTestCases")
    fun testCodeGen_interfaces(testCaseName: String) {
        val testPackage = "interfaces"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("miscTestCases")
    fun testCodeGen_misc(testCaseName: String) {
        val testPackage = "misc"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("projectionsTestCases")
    fun testCodeGen_projections(testCaseName: String) {
        val testPackage = "projections"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @ParameterizedTest
    @MethodSource("unionsTestCases")
    fun testCodeGen_unions(testCaseName: String) {
        val testPackage = "unions"
        val codeGenConfig = getBaseCodeGenConfig(testPackage, testCaseName)
        runTest(testPackage, testCaseName, codeGenConfig)
    }

    @Test
    fun `assert updateExpected is false`() {
        assertThat(updateExpected).isFalse()
    }

    private fun runTest(
        testPackage: String,
        testCaseName: String,
        codeGenConfig: CodeGenConfig,
    ) {
        val codeGenResult = CodeGen(codeGenConfig).generate()

        val generatedFilePathAndContentPairs =
            codeGenResult
                .javaSources()
                .map {
                    val generatedFilePath =
                        "${it.packageName()}.${it.typeSpec().name()}"
                            .replace(".", "/")
                            .plus(".java")

                    generatedFilePath to it.toString()
                }

        // fail if any file was defined twice
        generatedFilePathAndContentPairs
            .groupingBy { it.first }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .forEach { fail("Generated duplicate file: $it") }

        val expectedFileContentByPath =
            listAllFiles("$testPackage/$testCaseName/expected")
                .associate {
                    val relativePath = getTestsSourceTreeAbsolutePath().relativize(it).toString()
                    relativePath to it.readText()
                }

        // fail if any file was expected that's not generated
        expectedFileContentByPath
            .keys
            .subtract(generatedFilePathAndContentPairs.map { it.first }.toSet())
            .forEach { fail("Missing expected file: $it") }

        generatedFilePathAndContentPairs.forEach { (generatedFilePath, generatedContent) ->
            if (updateExpected) {
                writeExpected(generatedFilePath, generatedContent)
            } else {
                val expectedContent = expectedFileContentByPath[generatedFilePath]
                assertThat(generatedContent).isEqualTo(expectedContent)
            }
        }

        assertCompilesJava(codeGenResult)
    }

    companion object {
        @JvmStatic
        fun listTestsToRunForPackage(testPackageName: String): List<String> =
            getAbsolutePath(testPackageName)
                .listDirectoryEntries()
                .map { it.getName(it.nameCount.dec()).toString() }
                .sorted()

        @JvmStatic
        fun constantsTestCases(): List<String> = listTestsToRunForPackage("constants")

        @JvmStatic
        fun dataClassesTestCases(): List<String> = listTestsToRunForPackage("dataclasses")

        @JvmStatic
        fun enumsTestCases(): List<String> = listTestsToRunForPackage("enums")

        @JvmStatic
        fun inputsTestCases(): List<String> = listTestsToRunForPackage("inputs")

        @JvmStatic
        fun interfacesTestCases(): List<String> = listTestsToRunForPackage("interfaces")

        @JvmStatic
        fun miscTestCases(): List<String> = listTestsToRunForPackage("misc")

        @JvmStatic
        fun projectionsTestCases(): List<String> = listTestsToRunForPackage("projections")

        @JvmStatic
        fun unionsTestCases(): List<String> = listTestsToRunForPackage("unions")

        private fun getBaseCodeGenConfig(
            testPackage: String,
            testCaseName: String,
        ): CodeGenConfig {
            val schema = readFileContent("$testPackage/$testCaseName/schema.graphql")
            return CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.netflix.graphql.dgs.codegen.java.testcases.$testPackage.$testCaseName.expected",
                language = Language.JAVA,
                generateClientApi = true,
            )
        }

        private fun getAbsolutePath(suffix: String): Path {
            val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
            return Paths.get(projectDirAbsolutePath, "/src/integTest/java/com/netflix/graphql/dgs/codegen/java/testcases/$suffix")
        }

        private fun getTestsSourceTreeAbsolutePath(): Path {
            val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
            return Paths.get(projectDirAbsolutePath, "/src/integTest/java")
        }

        private fun listAllFiles(suffix: String): List<Path> {
            val path = getAbsolutePath(suffix)
            if (!path.exists()) return emptyList()
            return Files
                .walk(path)
                .filter { Files.isRegularFile(it) }
                .collect(Collectors.toList())
        }

        private fun readFileContent(fileName: String): String = getAbsolutePath(fileName).readText()

        private fun writeExpected(
            fileName: String,
            content: String,
        ) {
            val path = getTestsSourceTreeAbsolutePath().resolve(fileName)

            if (!path.exists()) {
                path.parent.createDirectories()
            }

            path.toFile().writeText(content)
        }
    }
}
