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

package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.test;

import com.netflix.graphql.dgs.client.codegen.GraphQLMultiQueryRequest;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.client.PersonGraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.client.PersonProjectionRoot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectionWithTypeAndArgsQueryTest {

    @Test
    void testQueryWithFragment() {
        var query = PersonGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .a2("name")
                .build();

        var projection = new PersonProjectionRoot<>()
                .firstname()
                .onEmployee().company();

        var request = new GraphQLQueryRequest(query, projection);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          person(a2: "name") {
                            firstname
                            ... on Employee {
                              __typename
                              company
                            }
                          }
                        }""");
    }

    @Test
    void testQueryWithUnnamedArgs() {
        var query = new PersonGraphQLQuery("a1", "a2", null, "TestQuery", Set.of());

        var projection = new PersonProjectionRoot<>()
                .firstname();

        var request = new GraphQLQueryRequest(query, projection);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          person(a1: "a1", a2: "a2") {
                            firstname
                          }
                        }""");
    }

    @Test
    void testQueryWithAlias() {
        var query1 = PersonGraphQLQuery.newRequest()
                .queryName("TestQuery1")
                .a2("person1")
                .build();
        query1.setQueryAlias("person1");

        var query2 = PersonGraphQLQuery.newRequest()
                .queryName("TestQuery2")
                .a2("person2")
                .build();
        query2.setQueryAlias("person2");

        var projection = new PersonProjectionRoot<>()
                .firstname();

        var request = new GraphQLMultiQueryRequest(List.of(
                new GraphQLQueryRequest(query1, projection),
                new GraphQLQueryRequest(query2, projection)
        ));

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery1 {
                          person1: person(a2: "person1") {
                            firstname
                          }
                          person2: person(a2: "person2") {
                            firstname
                          }
                        }""");
    }

}
