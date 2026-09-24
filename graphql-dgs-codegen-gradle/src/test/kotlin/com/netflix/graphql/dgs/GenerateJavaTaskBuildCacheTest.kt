/*
 *
 *  Copyright 2026 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.netflix.graphql.dgs

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.security.MessageDigest

class GenerateJavaTaskBuildCacheTest {
    @Test
    fun derivesOutputProvidersFromGeneratedSourcesDir(
        @TempDir tempDir: File,
    ) {
        val outputRoot = "build/custom-output"
        val projectDir = createProject(tempDir, "derived-output-providers", outputRoot, File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            tasks.register('verifyGeneratedDirectories') {
                doLast {
                    def codegen = tasks.named('generateJava').get()
                    assert codegen.generatedSourcesDirectory.get().asFile == file('$outputRoot/generated/sources/dgs-codegen')
                    assert codegen.generatedExamplesDirectory.get().asFile == file('$outputRoot/generated/sources/dgs-codegen-generated-examples')
                }
            }
            """.trimIndent(),
        )

        assertThat(run(projectDir, "verifyGeneratedDirectories").task(":verifyGeneratedDirectories")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun reusesCachedOutputsAcrossProjectAndOutputDirectories(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val firstProject = createProject(tempDir, "first-project", "build/first-output", cacheDir)
        val secondProject = createProject(tempDir, "second-project", "build/second-output", cacheDir)

        assertThat(run(firstProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        val firstOutput = generatedOutput(firstProject, "build/first-output")

        assertThat(run(secondProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(FROM_CACHE)
        val secondOutput = generatedOutput(secondProject, "build/second-output")

        assertThat(outputFingerprints(firstOutput)).isEqualTo(outputFingerprints(secondOutput))
    }

    @Test
    fun tracksSchemaAndConfigurationInputsAndUnchangedRuns(
        @TempDir tempDir: File,
    ) {
        val projectDir = createProject(tempDir, "input-tracking", "build/output", File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            generateJava {
                packageName = providers.gradleProperty('testPackage').getOrElse('com.netflix.testproject.graphql')
                generateClient = providers.gradleProperty('testClient').map { it.toBoolean() }.getOrElse(false)
                typeMapping = providers.gradleProperty('testMapping').isPresent() ? [Date: 'java.time.Instant'] : [Date: 'java.time.LocalDateTime']
                includeQueries = providers.gradleProperty('testQueries').isPresent() ? ['hello'] : []
            }
            """.trimIndent(),
        )

        assertSuccessfulExecution(run(projectDir, "--no-build-cache", "generateJava"))
        assertThat(run(projectDir, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(UP_TO_DATE)

        assertSuccessfulExecution(run(projectDir, "--no-build-cache", "-PtestPackage=com.netflix.changed", "generateJava"))
        assertSuccessfulExecution(
            run(projectDir, "--no-build-cache", "-PtestPackage=com.netflix.changed", "-PtestClient=true", "generateJava"),
        )
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "generateJava",
            ),
        )
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "-PtestQueries=true",
                "generateJava",
            ),
        )

        File(projectDir, "src/main/resources/schema/schema.graphqls").appendText("\n# invalidates the schema snapshot\n")
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "-PtestQueries=true",
                "generateJava",
            ),
        )
    }

    @Test
    fun doesNotCacheTimestampedGeneratedAnnotations(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val firstProject = createProject(tempDir, "first-dated-project", "build/output", cacheDir)
        val secondProject = createProject(tempDir, "second-dated-project", "build/output", cacheDir)
        val datedConfiguration =
            """

            generateJava {
                generatedAnnotationType = 'jakarta.annotation.Generated'
                disableDatesInGeneratedAnnotation = false
            }
            """.trimIndent()
        File(firstProject, "build.gradle").appendText(datedConfiguration)
        File(secondProject, "build.gradle").appendText(datedConfiguration)

        assertSuccessfulExecution(run(firstProject, "--build-cache", "generateJava"))
        assertSuccessfulExecution(run(secondProject, "--build-cache", "generateJava"))
    }

    private fun createProject(
        parent: File,
        name: String,
        outputRoot: String,
        cacheDir: File,
    ): File {
        val sourceDir = File("src/test/resources/test-project")
        val projectDir = File(parent, name).also { it.mkdirs() }
        val buildFile = File(sourceDir, "build.gradle").readText()
        File(projectDir, "build.gradle").writeText(
            buildFile.replace(
                "generatedSourcesDir = \"\${projectDir}/build/graphql\"",
                "generatedSourcesDir = file('$outputRoot').absolutePath",
            ),
        )
        File(sourceDir, "settings.gradle").copyTo(File(projectDir, "settings.gradle"))
        File(projectDir, "settings.gradle").appendText(
            """

            buildCache {
                local {
                    directory = file('${cacheDir.invariantSeparatorsPath}')
                }
            }
            """.trimIndent(),
        )
        File(sourceDir, "src").copyRecursively(File(projectDir, "src"))
        return projectDir
    }

    private fun run(
        projectDir: File,
        vararg arguments: String,
    ): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("--stacktrace", *arguments)
            .build()

    private fun assertSuccessfulExecution(result: BuildResult) {
        assertThat(result.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
    }

    private fun generatedOutput(
        projectDir: File,
        outputRoot: String,
    ): File = File(projectDir, "$outputRoot/generated/sources/dgs-codegen")

    private fun outputFingerprints(directory: File): Map<String, String> =
        directory
            .walkTopDown()
            .filter(File::isFile)
            .associate { file ->
                file.relativeTo(directory).invariantSeparatorsPath to
                    MessageDigest
                        .getInstance("SHA-256")
                        .digest(file.readBytes())
                        .joinToString("") { "%02x".format(it) }
            }
}
