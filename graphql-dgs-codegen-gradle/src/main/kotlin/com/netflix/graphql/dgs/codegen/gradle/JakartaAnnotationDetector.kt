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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier

object JakartaAnnotationDetector {
    private const val JAKARTA_ANNOTATION_GROUP = "jakarta.annotation"
    private const val JAKARTA_ANNOTATION_MODULE = "jakarta.annotation-api"
    private const val JAKARTA_ANNOTATION_MIN_MAJOR = 2 // jakarta.annotation.Generated introduced in 2.0.0

    fun detect(configuration: Configuration): Boolean =
        configuration.incoming
            .resolutionResult
            .allComponents
            .any { comp ->
                val id = comp.id
                id is ModuleComponentIdentifier &&
                    id.group == JAKARTA_ANNOTATION_GROUP &&
                    id.module == JAKARTA_ANNOTATION_MODULE &&
                    majorVersion(id.version) >= JAKARTA_ANNOTATION_MIN_MAJOR
            }

    private fun majorVersion(version: String): Int = version.takeWhile { it.isDigit() }.toIntOrNull() ?: -1
}
