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
                generateClientApi = true,
                maxProjectionDepth = 2,
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
                generateClientApi = true,
                maxProjectionDepth = 2,
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
}
