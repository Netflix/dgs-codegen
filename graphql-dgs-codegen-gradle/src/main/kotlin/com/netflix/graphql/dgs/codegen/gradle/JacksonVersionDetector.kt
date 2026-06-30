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

package com.netflix.graphql.dgs.codegen.gradle

import com.netflix.graphql.dgs.codegen.JacksonVersion
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

object JacksonVersionDetector {
    private const val JACKSON_2_GROUP = "com.fasterxml.jackson.core"
    private const val JACKSON_3_GROUP = "tools.jackson.core"
    private const val JACKSON_DATABIND_MODULE = "jackson-databind"

    /**
     * Which Jackson major versions are present in the resolved dependency graph reachable from [root]
     * (pass `resolutionResult.rootComponent.get()`).
     */
    fun detect(root: ResolvedComponentResult): Set<JacksonVersion> {
        val detected = mutableSetOf<JacksonVersion>()
        val visited = mutableSetOf<ComponentIdentifier>()
        val queue = ArrayDeque<ResolvedComponentResult>().apply { addLast(root) }

        while (queue.isNotEmpty()) {
            val component = queue.removeFirst()
            if (!visited.add(component.id)) continue

            component.moduleVersion?.let { module ->
                if (module.name == JACKSON_DATABIND_MODULE) {
                    when (module.group) {
                        JACKSON_2_GROUP -> detected.add(JacksonVersion.JACKSON_2)
                        JACKSON_3_GROUP -> detected.add(JacksonVersion.JACKSON_3)
                    }
                }
            }
            // Nothing more the graph can tell us once every known major has been seen.
            if (detected.size == JacksonVersion.entries.size) break

            component.dependencies
                .filterIsInstance<ResolvedDependencyResult>()
                .forEach { queue.addLast(it.selected) }
        }

        return detected
    }

    /**
     * Resolves the Jackson versions to target: the parsed [override] when non-empty, otherwise the
     * auto-[detected] set. An empty override means "no override" and falls back to detection.
     *
     * @throws InvalidUserDataException (failing the build) if [override] contains a value other than `"2"` or `"3"`.
     */
    fun resolve(
        override: List<String>,
        detected: Set<JacksonVersion>,
    ): Set<JacksonVersion> =
        if (override.isEmpty()) {
            detected
        } else {
            override
                .map { raw ->
                    try {
                        JacksonVersion.fromString(raw)
                    } catch (e: IllegalArgumentException) {
                        throw InvalidUserDataException("Invalid 'jacksonVersionOverride' value. ${e.message}", e)
                    }
                }.toSet()
        }
}
