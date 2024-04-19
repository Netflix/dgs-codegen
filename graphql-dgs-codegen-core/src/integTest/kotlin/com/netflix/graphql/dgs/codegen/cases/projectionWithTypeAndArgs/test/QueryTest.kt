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

package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.test

import com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.DgsClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QueryTest {

    @Test
    fun testQueryWithFragment() {
        val query = DgsClient.buildQuery {
            person(a2 = "name") {
                firstname
                onEmployee {
                    company
                }
            }
        }

        assertEquals(
            """{
            |  __typename
            |  person(a2: "name") {
            |    __typename
            |    firstname
            |    ... on Employee {
            |      __typename
            |      company
            |    }
            |  }
            |}
            |
            """.trimMargin(),
            query
        )
    }

    @Test
    fun testQueryWithAlias() {
        val query = DgsClient.buildQuery {
            person(_alias= "person1", a2 = "person1") {
                firstname
            }
            person(_alias= "person2", a2 = "person2") {
                firstname
            }
        }

        assertEquals(
            """{
            |  __typename
            |  person1: person(a2: "person1") {
            |    __typename
            |    firstname
            |  }
            |  person2: person(a2: "person2") {
            |    __typename
            |    firstname
            |  }
            |}
            |
            """.trimMargin(),
            query
        )
    }
}
