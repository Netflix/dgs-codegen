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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import kotlin.io.path.readText

class JavaCodeGenTest {
    // set this to true to update all expected outputs instead of running tests
    private val updateExpected = false

    @ParameterizedTest
    @MethodSource("listTestsToRun")
    fun testCodeGen(testCase: String) {
        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(casesRoot.resolve("$testCase/schema.graphql").readText()),
                    packageName = "com.netflix.graphql.dgs.codegen.java.testcases.${testCase.replace('/', '.')}.expected",
                    language = Language.JAVA,
                    generateClientApi = true,
                    typeMapping =
                        when (testCase) {
                            "dataclasses/dataClassWithMappedTypes" ->
                                mapOf(
                                    "Long" to "java.lang.Long",
                                    "DateTime" to "java.time.OffsetDateTime",
                                    "PageInfo" to "graphql.relay.PageInfo",
                                    "EntityConnection" to
                                        "graphql.relay.SimpleListConnection<com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.types.EntityEdge>",
                                )
                            "dataclasses/dataClassWithMappedInterfaces" ->
                                mapOf(
                                    "Node" to "com.netflix.graphql.dgs.codegen.java.fixtures.Node",
                                )
                            "inputs/inputWithDefaultBigDecimal" ->
                                mapOf(
                                    "Decimal" to "java.math.BigDecimal",
                                )
                            "inputs/inputWithDefaultCurrency" ->
                                mapOf(
                                    "Currency" to "java.util.Currency",
                                )
                            else -> emptyMap()
                        },
                ),
            ).generate()

        assertGeneratedSourcesMatchSnapshot(
            casesRoot.resolve("$testCase/expected"),
            codeGenResult.javaSources().map {
                sourceRoot.resolve(it.packageName().replace('.', '/')).resolve("${it.typeSpec().name()}.java") to it.toString()
            },
            updateExpected,
        )
        assertCompilesJava(codeGenResult)
    }

    @Test
    fun `assert updateExpected is false`() {
        assertThat(updateExpected).isFalse()
    }

    companion object {
        private val sourceRoot = Path.of("src/integTest/java")
        private val casesRoot = sourceRoot.resolve("com/netflix/graphql/dgs/codegen/java/testcases")

        @JvmStatic
        fun listTestsToRun(): List<String> = listSnapshotTestCases(casesRoot)
    }
}
