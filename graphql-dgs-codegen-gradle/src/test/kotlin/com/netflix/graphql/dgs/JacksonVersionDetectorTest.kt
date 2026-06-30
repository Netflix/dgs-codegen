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

package com.netflix.graphql.dgs

import com.netflix.graphql.dgs.codegen.JacksonVersion
import com.netflix.graphql.dgs.codegen.gradle.JacksonVersionDetector
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.gradle.api.InvalidUserDataException
import org.junit.jupiter.api.Test

class JacksonVersionDetectorTest {
    @Test
    fun `resolve falls back to the detected versions when no override is given`() {
        val detected = setOf(JacksonVersion.JACKSON_2)
        assertThat(JacksonVersionDetector.resolve(emptyList(), detected)).isEqualTo(detected)
    }

    @Test
    fun `resolve uses the override and ignores detection when an override is given`() {
        assertThat(JacksonVersionDetector.resolve(listOf("3"), setOf(JacksonVersion.JACKSON_2)))
            .containsExactly(JacksonVersion.JACKSON_3)
    }

    @Test
    fun `resolve supports targeting both Jackson versions`() {
        assertThat(JacksonVersionDetector.resolve(listOf("2", "3"), emptySet()))
            .containsExactlyInAnyOrder(JacksonVersion.JACKSON_2, JacksonVersion.JACKSON_3)
    }

    @Test
    fun `resolve rejects an unsupported override value with a build-failing user error`() {
        assertThatThrownBy { JacksonVersionDetector.resolve(listOf("4"), emptySet()) }
            .isInstanceOf(InvalidUserDataException::class.java)
            .hasMessageContaining("Invalid 'jacksonVersionOverride' value")
            .hasMessageContaining("Invalid Jackson version '4'")
    }
}
