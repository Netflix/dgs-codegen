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

import com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

/** Functional tests for the DGS codegen Gradle plugin. */
class CodegenGradlePluginTest {
    @TempDir
    lateinit var tempDir: File

    private val projectDir by lazy { copyFixture("test-project") }
    private val noSchemaProjectDir by lazy { copyFixture("test-project-no-schema-files") }
    private val multiModuleProjectDir by lazy { copyFixture("test-project-multimodule") }

    @AfterEach
    fun checkedInFixturesRemainClean() {
        val generatedDirectories =
            File("src/test/resources")
                .walkTopDown()
                .filter { it.isDirectory && (it.name == ".gradle" || it.name == "build") }
                .toList()

        assertThat(generatedDirectories).isEmpty()
    }

    @Test
    fun taskRegisteredSuccessfully() {
        // get a list of Gradle tasks
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "tasks",
                    "--all",
                ).build()

        // Verify the result
        assertThat(result.output).contains("generateJava")
    }

    @Test
    fun taskDependenciesRegisteredSuccessfully() {
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "copyMainSources",
                ).build()

        // Verify the result
        assertThat(result.task(":generateJava")).isNotNull
        assertThat(result.task(":generateJava")!!.outcome).isEqualTo(SUCCESS)
    }

    @ParameterizedTest
    @CsvSource(
        "build.gradle,build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types",
        "build_with_default_dir.gradle,build/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types",
    )
    fun sourcesGenerated(
        buildFile: String,
        outputPath: String,
    ) {
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=$buildFile",
                    "build",
                ).build()

        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(projectDir, "$outputPath/Result.java")).isFile()
    }

    @ParameterizedTest
    @CsvSource(
        "build.gradle,build/graphql/generated/sources/dgs-codegen",
        "build_with_default_dir.gradle,build/generated/sources/dgs-codegen",
    )
    fun onlySupportFilesAreGeneratedForNoSchema(
        buildFile: String,
        generatedSourcesPath: String,
    ) {
        // build a project
        val result =
            newGradleRunner()
                .withProjectDir(noSchemaProjectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=$buildFile",
                    "build",
                ).build()

        // Verify the result
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        val generatedSourcesDir = File(noSchemaProjectDir, generatedSourcesPath)
        val generatedFiles =
            generatedSourcesDir
                .walkTopDown()
                .filter { it.isFile }
                .map { it.relativeTo(generatedSourcesDir).invariantSeparatorsPath }
                .toList()
        assertThat(generatedFiles)
            .containsExactlyInAnyOrder(
                "com/netflix/testproject/graphql/DgsConstants.java",
                "com/netflix/testproject/graphql/Generated.java",
            )
    }

    @Test
    fun nonGraphQLFilesInSchemaDirectoryAreIgnored() {
        // build a project
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_default_dir.gradle",
                    "build",
                ).build()

        // Verify that the build succeeded.
        // This means there was no parsing error caused by the incorrect syntax in the schema.graphqlconfig file
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        // Verify that a NotSchema POJO has not been created
        // NotSchema is defined in notSchema.notgraphql, which has a non-GraphQL file extension but is a valid schema
        assertThat(File(defaultOutputDir, "Result.java")).exists()
        assertThat(File(defaultOutputDir, "NotSchema.java")).doesNotExist()
    }

    @Test
    fun jacksonVersionOverrideIsApplied() {
        // A valid override (["2", "3"]) should be accepted and supersede classpath detection.
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_jackson_override.gradle",
                    "generateJava",
                ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(generatedKotlinTypes())
            .contains("com.fasterxml.jackson.databind", "tools.jackson.databind")
    }

    @Test
    fun invalidJacksonVersionOverrideFailsTheBuild() {
        // An unsupported override value should fail the build with a clear message.
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_invalid_jackson_version.gradle",
                    "generateJava",
                ).buildAndFail()

        assertThat(result.output).contains("Invalid 'jacksonVersionOverride' value")
        assertThat(result.output).contains("Invalid Jackson version '4'")
    }

    @Test
    fun detectsJackson3FromCompileClasspath() {
        // End-to-end detection: Jackson 3 is the only Jackson on the compile classpath
        // and the generated Kotlin must use the tools.jackson annotation packages.
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_jackson3.gradle",
                    "generateJava",
                ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        val generatedTypes = generatedKotlinTypes()

        assertThat(generatedTypes).contains("tools.jackson.databind")
        assertThat(generatedTypes).doesNotContain("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "build_with_schema_paths_provider.gradle",
            "build_with_schema_paths_filecollection.gradle",
            "build_with_nested_schema_paths.gradle",
        ],
    )
    fun schemaPathsAcceptsConfiguredInputs(buildFile: String) {
        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=$buildFile",
                    "generateJava",
                ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(configuredOutputDir, "Result.java")).exists()
    }

    @Test
    fun getSchemaPathsReturnsAMutableListForBinaryCompatibility() {
        // Preserve the JVM getter descriptor and the mutable-list API used by downstream plugins.
        assertThat(GenerateJavaTask::class.java.getMethod("getSchemaPaths").returnType).isEqualTo(List::class.java)

        val result =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_schema_paths_addall.gradle",
                    "generateJava",
                ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(configuredOutputDir, "Result.java")).exists()
        assertThat(File(configuredOutputDir, "AppendedViaAddAll.java")).exists()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "build_with_nullable_classes.gradle",
            "build_with_schema_paths_filecollection.gradle",
            "build_with_schema_paths_provider.gradle",
        ],
    )
    fun generateJavaIsConfigurationCacheCompatible(buildFile: String) {
        assertConfigurationCacheRoundTrip(buildFile)
    }

    private fun assertConfigurationCacheRoundTrip(buildFile: String) {
        fun run() =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "--configuration-cache",
                    "--configuration-cache-problems=fail",
                    "-PtestBuildFile=$buildFile",
                    "clean",
                    "generateJava",
                ).build()

        File(projectDir, ".gradle/configuration-cache").deleteRecursively()

        val first = run()
        assertThat(first.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(first.output).contains("Configuration cache entry stored.")
        assertThat(File(configuredOutputDir, "Result.java")).exists()

        val second = run()
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(configuredOutputDir, "Result.java")).exists()
    }

    @Test
    fun generateJavaIsUpToDateWhenSchemaIsUnchanged() {
        // Keep verifying the content-based task avoidance provided by
        // @InputFiles after changing how schemaPaths accepts and resolves inputs.
        fun run(vararg tasks: String) =
            newGradleRunner()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments("--stacktrace", *tasks)
                .build()

        run("generateJava")
        assertThat(run("generateJava").task(":generateJava")).extracting { it?.outcome }.isEqualTo(UP_TO_DATE)

        // Modifying a schema file must invalidate the cached input snapshot.
        File(projectDir, "src/main/resources/schema/schema.graphqls").appendText("\n# touch\n")

        assertThat(run("generateJava").task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
    }

    @ParameterizedTest
    @ValueSource(strings = ["build.gradle", "build_with_relative_file.gradle"])
    fun schemaPathsResolveAgainstTheOwningProjectDirectory(buildFile: String) {
        val result =
            newGradleRunner()
                .withProjectDir(multiModuleProjectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestServerBuildFile=$buildFile",
                    ":server:generateJava",
                ).build()

        assertThat(result.task(":server:generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(multiModuleProjectDir, "server/$CONFIGURED_OUTPUT_PATH/Result.java")).isFile()
    }

    private val configuredOutputDir: File
        get() = File(projectDir, CONFIGURED_OUTPUT_PATH)

    private val defaultOutputDir: File
        get() = File(projectDir, DEFAULT_OUTPUT_PATH)

    private fun generatedKotlinTypes(): String =
        configuredOutputDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .joinToString("\n") { it.readText() }

    private fun newGradleRunner(): GradleRunner =
        GradleRunner
            .create()
            .withTestKitDir(File("build/tmp/test-kit", tempDir.name).absoluteFile)

    private fun copyFixture(name: String): File {
        val target = File(tempDir, name)
        check(File("src/test/resources", name).copyRecursively(target))
        return target
    }

    companion object {
        const val CONFIGURED_OUTPUT_PATH =
            "build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types"
        const val DEFAULT_OUTPUT_PATH =
            "build/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types"
    }
}
