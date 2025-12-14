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

package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithStringProperties.test;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithStringProperties.expected.client.PeopleGraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithStringProperties.expected.client.PeopleProjectionRoot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataClassQueryTest {

    @Test
    void testQuery() {
        var query = PeopleGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .build();

        var projection = new PeopleProjectionRoot<>()
                .firstname()
                .lastname();

        var request = new GraphQLQueryRequest(query, projection);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          people {
                            firstname
                            lastname
                          }
                        }""");
    }

}
