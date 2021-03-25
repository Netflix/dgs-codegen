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

package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.codegen.generators.shared.ClassnameShortener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ClassnameShortenerTest {
    @ParameterizedTest
    @MethodSource("inputAndExpectedProvider")
    fun shorten(input: String, expected: String) {
        val shortened = ClassnameShortener.shorten(input)
        assertThat(shortened).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun inputAndExpectedProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.arguments("This_Is_A_Test", "Th_Is_A_Te"),
                Arguments.arguments("T", "T"),
                Arguments.arguments("lowercase", "lo"),
                Arguments.arguments("lowercase_And_Uppercase", "lo_An_Up"),
            )
        }
    }
}
