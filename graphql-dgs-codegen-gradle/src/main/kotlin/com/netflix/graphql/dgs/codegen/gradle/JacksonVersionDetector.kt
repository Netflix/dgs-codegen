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
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

object JacksonVersionDetector {
    private const val JACKSON_2_GROUP = "com.fasterxml.jackson.core"
    private const val JACKSON_3_GROUP = "tools.jackson.core"
    private const val JACKSON_DATABIND_MODULE = "jackson-databind"

    /**
     * Inspect a configuration's resolved components and report which Jackson major versions
     * are on the classpath. Uses `incoming.resolutionResult` rather than
     * `resolvedConfiguration.resolvedArtifacts` to avoid eager artifact download, which can
     * break plugins (e.g. the Jakarta EE migration plugin) and multi-module projects.
     */
    fun detect(configuration: Configuration): Set<JacksonVersion> {
        val versions = mutableSetOf<JacksonVersion>()
        configuration.incoming.resolutionResult.allComponents.forEach { comp ->
            val id = comp.id
            if (id !is ModuleComponentIdentifier) return@forEach
            if (id.module != JACKSON_DATABIND_MODULE) return@forEach
            when (id.group) {
                JACKSON_2_GROUP -> versions += JacksonVersion.JACKSON_2
                JACKSON_3_GROUP -> versions += JacksonVersion.JACKSON_3
            }
        }
        return versions
    }
}
