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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

internal fun listSnapshotTestCases(root: Path): List<String> =
    Files.walk(root).use { paths ->
        paths
            .filter { it.fileName.toString() == "schema.graphql" }
            .map { root.relativize(it.parent).joinToString("/") }
            .sorted()
            .toList()
    }

internal fun assertGeneratedSourcesMatchSnapshot(
    expectedDirectory: Path,
    generatedSources: List<Pair<Path, String>>,
    updateExpected: Boolean,
) {
    val generatedPaths = generatedSources.map { it.first }
    assertThat(generatedPaths).`as`("Generated source paths").doesNotHaveDuplicates()

    val expectedPaths =
        if (expectedDirectory.exists()) {
            Files.walk(expectedDirectory).use { paths -> paths.filter { Files.isRegularFile(it) }.toList() }
        } else {
            emptyList()
        }

    if (updateExpected) {
        // Updating snapshots must not hide files that the generator stopped producing.
        assertThat(generatedPaths).`as`("Generated sources in %s", expectedDirectory).containsAll(expectedPaths)
    } else {
        assertThat(generatedPaths)
            .`as`("Generated sources in %s", expectedDirectory)
            .containsExactlyInAnyOrderElementsOf(expectedPaths)
    }

    generatedSources.forEach { (path, content) ->
        if (updateExpected) {
            path.parent.createDirectories()
            path.writeText(content)
        } else {
            assertThat(content).`as`("Generated source %s", path).isEqualTo(path.readText())
        }
    }
}
