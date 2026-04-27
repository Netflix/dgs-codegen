/*
 *
 *  Copyright 2026 Netflix, Inc.
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

package com.netflix.graphql.dgs.codegen

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

class CodeGenSnapshotsTest {
    @TempDir
    lateinit var expectedDirectory: Path

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `duplicate paths fail before snapshots are overwritten`(updateExpected: Boolean) {
        val path = expectedDirectory.resolve("Duplicate.java")
        path.writeText("original")

        assertThatThrownBy {
            assertGeneratedSourcesMatchSnapshot(expectedDirectory, listOf(path to "first", path to "second"), updateExpected)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Duplicate.java")
        assertThat(path.readText()).isEqualTo("original")
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `missing generated files fail before snapshots are overwritten`(updateExpected: Boolean) {
        val path = expectedDirectory.resolve("Present.java")
        path.writeText("original")
        expectedDirectory.resolve("Missing.java").writeText("missing")

        assertThatThrownBy {
            assertGeneratedSourcesMatchSnapshot(expectedDirectory, listOf(path to "changed"), updateExpected)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Missing.java")
        assertThat(path.readText()).isEqualTo("original")
    }

    @Test
    fun `unexpected generated files fail`() {
        val path = expectedDirectory.resolve("Unexpected.java")

        assertThatThrownBy {
            assertGeneratedSourcesMatchSnapshot(expectedDirectory, listOf(path to "unexpected"), updateExpected = false)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Unexpected.java")
        assertThat(path).doesNotExist()
    }

    @Test
    fun `changed generated content fails`() {
        val path = expectedDirectory.resolve("Changed.java")
        path.writeText("original")

        assertThatThrownBy {
            assertGeneratedSourcesMatchSnapshot(expectedDirectory, listOf(path to "changed"), updateExpected = false)
        }.isInstanceOf(AssertionError::class.java)
            .hasMessageContaining("Changed.java")
    }
}
