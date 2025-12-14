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

package com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.test;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.expected.client.MoviesGraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.expected.types.MovieFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InputQueryTest {

    @Test
    void testQueryWithNoFilter() {
        var query = MoviesGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          movies
                        }""");
    }

    @Test
    void testQueryWithEmptyFilter() {
        var query = MoviesGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .filter(new MovieFilter())
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          movies(filter: {})
                        }""");
    }

    @Test
    void testQueryWithFilter() {
        var query = MoviesGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .filter(MovieFilter.newBuilder()
                        .genre("horror")
                        .build())
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          movies(filter: {genre : "horror"})
                        }""");
    }

    @Test
    void testQueryWithNewline() {
        var query = MoviesGraphQLQuery.newRequest()
                .queryName("TestQuery")
                .filter(MovieFilter.newBuilder()
                        .genre("horror\ncomedy")
                        .build())
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          movies(filter: {genre : "horror\\ncomedy"})
                        }""");
    }

}
