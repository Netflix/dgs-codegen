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

package com.netflix.graphql.dgs.codegen.generators.shared

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class CodeGeneratorUtilsTest {

    @ParameterizedTest(name = "{index} => {0}, expected {1}")
    @MethodSource("toSnakeCaseData")
    fun toSnakeCase(input: String, output: String) {
        assertThat(CodeGeneratorUtils.camelCaseToSnakeCase(input)).isEqualTo(output)
    }

    companion object {
        @JvmStatic
        fun toSnakeCaseData(): Stream<Arguments> = Stream.of(
            "abc" to "abc",
            "ABC" to "abc",
            "Abc" to "abc",
            "1AB" to "1_ab",
            "1Abc" to "1_abc",
            "ABCefg" to "ab_cefg",
            "A1BCefg" to "a_1_b_cefg",
            "AbCeFg" to "ab_ce_fg",
        ).map { Arguments.of(it.first, it.second) }
    }
}
