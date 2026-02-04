/*
 *
 *  Copyright 2025 Netflix, Inc.
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
package com.netflix.graphql.dgs.codegen.clientapi

import com.netflix.graphql.dgs.codegen.*
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class ClientApiGenReservedKeywordTest {
    @Test
    fun `Field named package should be sanitized in generated client API`() {
        @Language("GraphQL")
        val schema =
            """
            type Query {
                something: Something
            }
            
            type Something {
                package: String
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.netflix.graphql.dgs.codegen.tests.generated",
                    generateClientApi = true,
                ),
            ).generate()

        assertThat(codeGenResult.javaDataTypes).hasSize(1)
        assertThat(codeGenResult.clientProjections).hasSize(1)

        // Check that the field name is properly sanitized in the generated data type
        val dataType = codeGenResult.javaDataTypes[0].typeSpec()
        val fieldNames = dataType.fieldSpecs().map { it.name() }
        assertThat(fieldNames).contains("_package")
        assertThat(fieldNames).doesNotContain("package")
    }

    @Test
    fun `Query field with argument named package should generate valid code`() {
        @Language("GraphQL")
        val schema =
            """
            type Query {
                something(package: String): String
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.netflix.graphql.dgs.codegen.tests.generated",
                    generateClientApi = true,
                ),
            ).generate()

        // Verify the generated code compiles without errors
        assertCompilesJava(
            codeGenResult.clientProjections +
                codeGenResult.javaQueryTypes +
                codeGenResult.javaEnumTypes +
                codeGenResult.javaDataTypes +
                codeGenResult.javaInterfaces,
        )
    }

    @Test
    fun `Variable reference method should use original GraphQL field name as map key`() {
        @Language("GraphQL")
        val schema =
            """
            type Query {
                something(package: String): String
            }
            """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "com.netflix.graphql.dgs.codegen.tests.generated",
                    generateClientApi = true,
                ),
            ).generate()

        // Find the SomethingGraphQLQuery type and its Builder class
        val queryType = codeGenResult.javaQueryTypes.first()
        val builderClass =
            queryType.typeSpec().typeSpecs().find { it.name() == "Builder" }
                ?: throw AssertionError("Builder class not found")

        // Find the _packageReference method
        val referenceMethod =
            builderClass.methodSpecs().find { it.name() == "_packageReference" }
                ?: throw AssertionError("_packageReference method not found")

        // Verify the method uses original GraphQL name "package" as the key, not "_package"
        val methodCode = referenceMethod.code().toString()
        assertThat(methodCode).contains("variableReferences.put(\"package\"")
        assertThat(methodCode).doesNotContain("variableReferences.put(\"_package\"")
    }
}
