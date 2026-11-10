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
import graphql.parser.Parser;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Locale;
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
    void testMultipleQueriesWithVariableReferences() {
        var query1 = PersonGraphQLQuery.newRequest()
                .queryName("Combined")
                .a1("literal")
                .a2Reference("firstName")
                .build();
        query1.setQueryAlias("first");

        var query2 = PersonGraphQLQuery.newRequest()
                .a2Reference("secondName")
                .build();
        query2.setQueryAlias("second");

        var projection = new PersonProjectionRoot<>().firstname();
        var request = new GraphQLMultiQueryRequest(List.of(
                new GraphQLQueryRequest(query1, projection),
                new GraphQLQueryRequest(query2, projection)
        ));

        assertThat(request.serialize()).isEqualTo("""
                query Combined($firstName: String!, $secondName: String!) {
                  first: person(a1: "literal", a2: $firstName) {
                    firstname
                  }
                  second: person(a2: $secondName) {
                    firstname
                  }
                }""");
    }

    @Test
    void testMultipleQueriesWithSharedVariableReferencesValidate() {
        var query1 = PersonGraphQLQuery.newRequest()
                .a1Reference("shared")
                .a2("literal")
                .build();
        query1.setQueryAlias("first");

        var query2 = PersonGraphQLQuery.newRequest()
                .a1Reference("shared")
                .a2("literal")
                .build();
        query2.setQueryAlias("second");

        var projection = new PersonProjectionRoot<>().firstname();
        var request = new GraphQLMultiQueryRequest(List.of(
                new GraphQLQueryRequest(query1, projection),
                new GraphQLQueryRequest(query2, projection)
        ));
        var schema = new SchemaGenerator().makeExecutableSchema(
                new SchemaParser().parse("""
                        type Query { person(a1: String, a2: String!): Person }
                        type Person { firstname: String }
                        """),
                RuntimeWiring.newRuntimeWiring().build()
        );

        assertThat(new Validator().validateDocument(schema, new Parser().parseDocument(request.serialize()), Locale.ROOT))
                .isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void testSharedVariableWithNullableAndNonNullArguments(boolean reverseOrder) {
        var nullable = PersonGraphQLQuery.newRequest().a1Reference("shared").a2("literal").build();
        nullable.setQueryAlias("nullable");
        var nonNull = PersonGraphQLQuery.newRequest().a2Reference("shared").build();
        nonNull.setQueryAlias("nonNull");
        var projection = new PersonProjectionRoot<>().firstname();
        var nullableRequest = new GraphQLQueryRequest(nullable, projection);
        var nonNullRequest = new GraphQLQueryRequest(nonNull, projection);
        var requests = reverseOrder
                ? List.of(nonNullRequest, nullableRequest)
                : List.of(nullableRequest, nonNullRequest);
        var serialized = new GraphQLMultiQueryRequest(requests).serialize();
        var schema = new SchemaGenerator().makeExecutableSchema(
                new SchemaParser().parse("""
                        type Query { person(a1: String, a2: String!): Person }
                        type Person { firstname: String }
                        """),
                RuntimeWiring.newRuntimeWiring().build()
        );

        assertThat(new Validator().validateDocument(schema, new Parser().parseDocument(serialized), Locale.ROOT))
                .isEmpty();
        assertThat(serialized).contains("query ($shared: String!)");
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
