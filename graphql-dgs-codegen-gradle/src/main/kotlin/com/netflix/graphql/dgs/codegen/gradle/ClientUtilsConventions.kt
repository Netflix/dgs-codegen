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

import nebula.plugin.dependencylock.DependencyLockExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import java.io.FileNotFoundException
import java.util.Optional
import java.util.Properties

object ClientUtilsConventions {
    const val GRADLE_CLASSPATH_CONFIGURATION = "implementation"

    private const val CLIENT_UTILS_ARTIFACT_GROUP = "com.netflix.graphql.dgs.codegen"
    private const val CLIENT_UTILS_ARTIFACT_NAME = "graphql-dgs-codegen-shared-core"
    private const val CLIENT_UTILS_NEBULA_LOCK_ID = "com.netflix.nebula.dependency-lock"

    private val logger = Logging.getLogger(ClientUtilsConventions::class.java)

    fun apply(
        project: Project,
        optionalCodeUtilsVersion: Optional<String> = Optional.empty(),
        optionalCodeClientDependencyScope: Optional<String> = Optional.empty()
    ) {
        clientCoreArtifact(optionalCodeUtilsVersion).ifPresent { dependencyString ->
            val dependencyLockString = dependencyString.split(":").take(2).joinToString(":")

            val dependencyConfiguration = optionalCodeClientDependencyScope.orElse(GRADLE_CLASSPATH_CONFIGURATION)
            val configurationDependencies = project.configurations.getByName(dependencyConfiguration).dependencies
            configurationDependencies.add(project.dependencies.create(dependencyString))
            logger.info("DGS CodeGen added [{}] to the {} dependencies.", dependencyString, dependencyConfiguration)

            project.plugins.withId(CLIENT_UTILS_NEBULA_LOCK_ID) {
                val extension = project.extensions.getByType(DependencyLockExtension::class.java)
                if (extension != null) {
                    extension.skippedDependencies.add(dependencyLockString)
                }

                project.dependencyLocking.ignoredDependencies.add(dependencyLockString)
            }
        }
    }

    private val pluginProperties: Optional<Properties> = try {
        val props = Properties()
        val inputStream = this.javaClass.classLoader.getResourceAsStream("META-INF/graphql-dgs-codegen-core.properties")
            ?: throw FileNotFoundException("property file not found in the classpath")
        inputStream.use { props.load(it) }
        Optional.of(props)
    } catch (e: Exception) {
        logger.error("Unable to resolve the graphql-dgs-codegen-gradle.properties properties.")
        Optional.empty()
    }

    internal val pluginMetaInfVersion: Optional<String> =
        pluginProperties.flatMap { Optional.ofNullable(it.getProperty("Implementation-Version")) }

    private fun clientCoreArtifact(optionalVersion: Optional<String>): Optional<String> {
        val version = if (optionalVersion.isPresent) optionalVersion else pluginMetaInfVersion
        return version.map { "$CLIENT_UTILS_ARTIFACT_GROUP:$CLIENT_UTILS_ARTIFACT_NAME:$it" }
    }
}
