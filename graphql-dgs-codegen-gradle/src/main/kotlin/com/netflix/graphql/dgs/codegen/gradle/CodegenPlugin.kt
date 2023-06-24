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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.util.GradleVersion
import java.util.Optional

class CodegenPlugin : Plugin<Project> {

    companion object {
        const val GRADLE_GROUP = "DGS GraphQL Codegen"
        private val logger = Logging.getLogger(CodegenPlugin::class.java)
    }

    override fun apply(project: Project) {
        val codegenExtension = project.extensions.create("codegen", CodegenPluginExtension::class.java)

        project.plugins.apply(JavaPlugin::class.java)

        val generateJavaTaskProvider = project.tasks.register("generateJava", GenerateJavaTask::class.java)
        generateJavaTaskProvider.configure { it.group = GRADLE_GROUP }

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val javaExtension = project.extensions.getByType(JavaPluginExtension::class.java)

        val sourceSets = if (GradleVersion.current() >= GradleVersion.version("7.1")) javaExtension.sourceSets else javaConvention.sourceSets
        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val outputDir = generateJavaTaskProvider.map(GenerateJavaTask::getOutputDir)
        mainSourceSet.java.srcDirs(project.files(outputDir).builtBy(generateJavaTaskProvider))

        project.configurations.create("dgsCodegen")
        project.configurations.findByName("dgsCodegen")?.isCanBeResolved = true

        addDependencyLock(project, codegenExtension)

        project.afterEvaluate { p ->
            if (codegenExtension.clientCoreConventionsEnabled.getOrElse(true)) {
                logger.info("Applying CodegenPlugin Client Utils conventions.")
                ClientUtilsConventions.apply(
                    p,
                    Optional.ofNullable(codegenExtension.clientCoreVersion.orNull),
                    Optional.ofNullable(codegenExtension.clientCoreScope.orNull)
                )
            }
        }
    }

    private fun addDependencyLock(project: Project, codegenExtension: CodegenPluginExtension) {
        val dependencyLockString = ClientUtilsConventions.getDependencyString()
        try {
            if (codegenExtension.clientCoreConventionsEnabled.getOrElse(true)) {
                project.dependencyLocking.ignoredDependencies.add(dependencyLockString)
                logger.info(
                    "DGS CodeGen added ignored dependency [{}].",
                    dependencyLockString
                )
            }
        } catch (e: Exception) {
            // do nothing, this is supplemental and seems to work in certain contexts but not in others
            logger.info(
                "Failed to add DGS CodeGen to ignoredDependencies because: {}",
                dependencyLockString,
                e
            )
        }
    }
}
