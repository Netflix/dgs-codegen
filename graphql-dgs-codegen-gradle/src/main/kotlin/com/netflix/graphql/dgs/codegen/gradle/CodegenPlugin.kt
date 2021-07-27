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
import org.gradle.api.tasks.SourceSet
import java.util.*

class CodegenPlugin : Plugin<Project> {

    companion object {
        const val GRADLE_GROUP = "DGS GraphQL Codegen"
        private val logger = Logging.getLogger(CodegenPlugin::class.java)
    }

    override fun apply(project: Project) {
        val extensions = project.extensions.create("codegen", CodegenPluginExtension::class.java)

        project.plugins.apply(JavaPlugin::class.java)

        val generateJavaTaskProvider = project.tasks.register("generateJava", GenerateJavaTask::class.java)
        generateJavaTaskProvider.configure { it.group = GRADLE_GROUP }

        project.getTasksByName("compileJava", false).forEach {
            it.dependsOn(generateJavaTaskProvider.get())
        }
        project.getTasksByName("compileKotlin", false).forEach {
            it.dependsOn(generateJavaTaskProvider.get())
        }

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSets = javaConvention.sourceSets
        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val outputDir = generateJavaTaskProvider.get().getOutputDir()
        mainSourceSet.java.srcDirs(outputDir)

        project.afterEvaluate { p ->
            if (extensions.clientCoreConventionsEnabled.getOrElse(true)) {
                logger.info("Applying CodegenPlugin Client Utils conventions.")
                ClientUtilsConventions.apply(
                    p,
                    Optional.ofNullable(extensions.clientCoreVersion.orNull),
                    Optional.ofNullable(extensions.clientCoreScope.orNull)
                )
            }
        }
    }
}
