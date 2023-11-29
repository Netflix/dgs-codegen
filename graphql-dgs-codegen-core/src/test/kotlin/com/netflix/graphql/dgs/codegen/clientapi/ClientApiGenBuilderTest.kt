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

package com.netflix.graphql.dgs.codegen.clientapi

import com.netflix.graphql.dgs.client.codegen.GraphQLQuery
import com.netflix.graphql.dgs.codegen.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ClientApiGenBuilderTest {
    @Test
    fun `Fields explicitly set to null in the builder should be included`() {
        val schema = """
            type Query {
                filter(nameFilter: String): [String]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                maxProjectionDepth = 2
            )
        ).generate()

        val builderClass = assertCompilesJava(codeGenResult).toClassLoader()
            .loadClass("$basePackageName.client.FilterGraphQLQuery\$Builder")

        val buildMethod = builderClass.getMethod("build")
        val nameMethod = builderClass.getMethod("nameFilter", String::class.java)

        // When the 'nameFilter' method is invoked with a null value, the field should be included in the input map and explicitly set to null.
        val builder1 = builderClass.constructors[0].newInstance()
        nameMethod.invoke(builder1, null)
        val resultQueryObject: GraphQLQuery = buildMethod.invoke(builder1) as GraphQLQuery
        assertThat(resultQueryObject.input.keys).containsExactly("nameFilter")
        assertThat(resultQueryObject.input["nameFilter"]).isNull()
    }

    @Test
    fun `Fields not explicitly set to null or any value in the builder should not be included`() {
        val schema = """
            type Query {
                filter(nameFilter: String): [String]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                maxProjectionDepth = 2
            )
        ).generate()

        val builderClass = assertCompilesJava(codeGenResult).toClassLoader()
            .loadClass("$basePackageName.client.FilterGraphQLQuery\$Builder")
        val buildMethod = builderClass.getMethod("build")

        // When the 'nameFilter' method is not invoked, it should not be included in the input map.
        val builder2 = builderClass.constructors[0].newInstance()
        val result2QueryObject: GraphQLQuery = buildMethod.invoke(builder2) as GraphQLQuery
        assertThat(result2QueryObject.input.keys).isEmpty()
        assertThat(result2QueryObject.input["nameFilter"]).isNull()
    }

    @Test
    fun `Query name should be null if not set`() {
        val schema = """
            type Query {
                filter(nameFilter: String): [String]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                maxProjectionDepth = 2
            )
        ).generate()

        val builderClass = assertCompilesJava(codeGenResult).toClassLoader()
            .loadClass("$basePackageName.client.FilterGraphQLQuery\$Builder")
        val buildMethod = builderClass.getMethod("build")

        val builder = builderClass.constructors[0].newInstance()
        val result2QueryObject: GraphQLQuery = buildMethod.invoke(builder) as GraphQLQuery
        assertThat(result2QueryObject.name).isNull()
    }

    @Test
    fun `Query name should be accessible via GraphQLQuery#name if set`() {
        val schema = """
            type Query {
                filter(nameFilter: String): [String]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApiv2 = true,
                maxProjectionDepth = 2
            )
        ).generate()

        val builderClass = assertCompilesJava(codeGenResult).toClassLoader()
            .loadClass("$basePackageName.client.FilterGraphQLQuery\$Builder")
        val nameMethod = builderClass.getMethod("queryName", String::class.java)
        val buildMethod = builderClass.getMethod("build")

        val builder = builderClass.constructors[0].newInstance()
        nameMethod.invoke(builder, "test")

        val result2QueryObject: GraphQLQuery = buildMethod.invoke(builder) as GraphQLQuery
        assertThat(result2QueryObject.name).isNotNull
        assertThat(result2QueryObject.name).isEqualTo("test")
    }

    @Nested
    inner class Deprecation {

        @Test
        fun `adds @Deprecated annotation and reason from schema directives when setting enabled`() {
            val schema = """
                type Query {
                    filter(
                        nameFilter: String @deprecated(reason: "use idFilter instead"),
                        idFilter: ID
                    ): [String]
                }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApiv2 = true,
                    maxProjectionDepth = 2,
                    addDeprecatedAnnotation = true
                )
            ).generate()

            assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("FilterGraphQLQuery")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs).hasSize(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs).hasSize(4)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("nameFilter")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].toString()).startsWith(
                """
                    |/**
                    | * @deprecated use idFilter instead
                    | */
                    |@java.lang.Deprecated
                """.trimMargin()
            )
        }

        @Test
        fun `adds @Deprecated annotation without a Javadoc when there is no reason`() {
            val schema = """
                type Query {
                    filter(
                        nameFilter: String @deprecated,
                        idFilter: ID
                    ): [String]
                }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApiv2 = true,
                    maxProjectionDepth = 2,
                    addDeprecatedAnnotation = true
                )
            ).generate()

            assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("FilterGraphQLQuery")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs).hasSize(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs).hasSize(4)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("nameFilter")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].toString()).startsWith(
                """
                    |@java.lang.Deprecated
                """.trimMargin()
            )
        }

        @Test
        fun `Deprecation reason and field's description go both into JavaDoc separated by an empty line`() {
            val schema = """
                type Query {
                    filter(
                        ${"\"\"\""}
                        Filters by name.
                        
                        If not provided, no filter in regards to name is applied.
                        ${"\"\"\""}
                        nameFilter: String @deprecated(reason: "use idFilter instead"),
                        idFilter: ID
                    ): [String]
                }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApiv2 = true,
                    maxProjectionDepth = 2,
                    addDeprecatedAnnotation = true
                )
            ).generate()

            assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("FilterGraphQLQuery")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs).hasSize(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs).hasSize(4)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("nameFilter")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].toString()).startsWith(
                """
                    |/**
                    | * Filters by name.
                    | *         
                    | * If not provided, no filter in regards to name is applied.
                    | *
                    | * @deprecated use idFilter instead
                    | */
                    |@java.lang.Deprecated
                """.trimMargin()
            )
        }

        @Test
        fun `adds nothing extra when addDeprecatedAnnotation is not enabled`() {
            val schema = """
                type Query {
                    filter(
                        nameFilter: String @deprecated(reason: "use idFilter instead"),
                        idFilter: ID
                    ): [String]
                }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateClientApiv2 = true,
                    maxProjectionDepth = 2
                )
            ).generate()

            assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("FilterGraphQLQuery")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs).hasSize(1)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs).hasSize(4)
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("nameFilter")
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].annotations).isEmpty()
            assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].javadoc.isEmpty).isTrue()
        }
    }
}
