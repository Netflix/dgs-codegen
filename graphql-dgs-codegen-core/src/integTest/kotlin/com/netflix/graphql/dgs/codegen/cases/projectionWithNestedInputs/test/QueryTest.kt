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

package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.test

import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.DgsClient
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I1
import com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected.types.I2
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun testWipeSameNameDifferentClass() {
        val query = DgsClient.buildQuery {
            q1(
                arg2 = I2(
                    arg2 = ""
                )
            )
        }

        Assertions.assertEquals(
            """query {
            |  __typename
            |  q1(arg2: {arg2 : ""})
            |}
            |""".trimMargin(), query
        )
    }

    @Test
    fun testWipeSameNameSameClass() {
        val query = DgsClient.buildQuery {
            q2(
                arg1 = I1(
                    arg1 = I1(
                    )
                ),
            )
        }

        Assertions.assertEquals(
            """query {
            |  __typename
            |  q2(arg1: {arg1 : {}})
            |}
            |""".trimMargin(), query
        )
    }
}
