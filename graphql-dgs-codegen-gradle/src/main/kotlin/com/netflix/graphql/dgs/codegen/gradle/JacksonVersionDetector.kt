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
import org.gradle.api.Project
import org.gradle.api.logging.Logging

object JacksonVersionDetector {
    private val logger = Logging.getLogger(JacksonVersionDetector::class.java)

    /**
     * Detects which Jackson versions are present in the project's compile classpath.
     */
    fun detectVersions(project: Project): Set<JacksonVersion> {
        val configurationsToCheck =
            listOfNotNull(
                project.configurations.findByName("compileClasspath"),
            ).filter { it.isCanBeResolved }

        val result = mutableSetOf<JacksonVersion>()

        for (configuration in configurationsToCheck) {
            try {
                val dependencies = configuration.resolvedConfiguration.resolvedArtifacts
                for (artifact in dependencies) {
                    val id = artifact.moduleVersion.id
                    if (id.group == "com.fasterxml.jackson.core" && id.name == "jackson-databind") {
                        val version = id.version
                        logger.info("DGS Codegen: Found Jackson 2 ($version) in ${configuration.name}")
                        result.add(JacksonVersion.JACKSON_2)
                    }
                    if (id.group == "tools.jackson.core" && id.name == "jackson-databind") {
                        logger.info("DGS Codegen: Found Jackson 3 (${id.version}) in ${configuration.name}")
                        result.add(JacksonVersion.JACKSON_3)
                    }
                }
            } catch (e: Exception) {
                logger.debug("Could not resolve configuration ${configuration.name}: ${e.message}")
            }
        }

        if (result.size == 2) {
            logger.info("DGS Codegen: Both Jackson 2 and 3 detected in project dependencies. Will generate annotations for both.")
        } else if (result.isEmpty()) {
            logger.warn("DGS Codegen: Could not detect Jackson version from project dependencies. Defaulting to Jackson 2 annotations.")
        }

        return result
    }
}
