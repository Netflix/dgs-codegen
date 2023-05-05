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

import org.assertj.core.api.Assertions
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CodegenGradlePluginConfigurationCacheTest {

    @TempDir
    lateinit var projectDir: File

    @Test
    fun `Test if configuration cache can be reused successfully`() {
        prepareBuildGraphQLSchema(
            """
                 type Query {
                     test: String
                 }
            """.trimMargin()
        )

        prepareBuildGradleFile(
            """
                plugins {
                    id 'java'
                    id 'com.netflix.dgs.codegen'
                }
                
                 repositories {
                	mavenCentral()
                }

                sourceCompatibility = 1.8
                targetCompatibility = 1.8

                 generateJava {
                     packageName = 'com.netflix.testproject.graphql'
                     generateClient = true
                }

                // Need to disable the core conventions since the artifacts are not yet visible.
                codegen.clientCoreConventionsEnabled = false
            """.trimMargin()
        )

        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(
                "--stacktrace",
                "--info",
                "--configuration-cache",
                "generateJava",
                "build"
            )

        runner.build() // First build, warm up cache
        val result = runner.build() // Second build, should use cache

        Assertions.assertThat(result.output.contains("Reusing configuration cache."))
    }

    private fun prepareBuildGradleFile(content: String) {
        writeProjectFile("build.gradle", content)
    }

    private fun prepareBuildGraphQLSchema(content: String) {
        writeProjectFile("src/main/resources/schema/schema.graphql", content)
    }

    private fun writeProjectFile(relativePath: String, content: String) {
        val file = File(projectDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}
