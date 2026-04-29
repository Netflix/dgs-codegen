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

package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.test;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.client.Q1GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.client.Q2GraphQLQuery;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.types.I1;
import com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.types.I2;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectionWithNestedInputsQueryTest {

    @Test
    void testWipeSameNameDifferentClass() {
        var query = Q1GraphQLQuery.newRequest()
                .queryName("TestQuery")
                .arg2(I2.newBuilder()
                        .arg2("")
                        .build())
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          q1(arg2: {arg2 : ""})
                        }""");
    }

    @Test
    void testWipeSameNameSameClass() {
        var query = Q2GraphQLQuery.newRequest()
                .queryName("TestQuery")
                .arg1(I1.newBuilder()
                        .arg1(new I1())
                        .build())
                .build();

        var request = new GraphQLQueryRequest(query);

        assertThat(request.serialize())
                .isEqualTo("""
                        query TestQuery {
                          q2(arg1: {arg1 : {}})
                        }""");
    }

}
