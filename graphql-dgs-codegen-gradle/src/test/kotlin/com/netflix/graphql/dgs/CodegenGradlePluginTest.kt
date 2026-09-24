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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/** Functional tests for the DGS codegen Gradle plugin. */
class CodegenGradlePluginTest {
    @Test
    fun taskRegisteredSuccessfully() {
        // get a list of Gradle tasks
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "tasks",
                    "--all",
                ).forwardOutput()
                .build()

        // Verify the result
        assertThat(result.output).contains("generateJava")
    }

    @Test
    fun taskDependenciesRegisteredSuccessfully() {
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "clean",
                    "copyMainSources",
                ).forwardOutput()
                .build()

        // Verify the result
        assertThat(result.task(":generateJava")).isNotNull
        assertThat(result.task(":generateJava")!!.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun sourcesGenerated() {
        // build a project
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "clean",
                    "build",
                ).forwardOutput()
                .withDebug(true)
                .build()

        // Verify the result
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        // Verify that POJOs are generated in the configured directory
        assertThat(File(EXPECTED_PATH + "Result.java").exists()).isTrue
    }

    @Test
    fun sourcesGenerated_UsingDefaultPath() {
        // build a project
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_default_dir.gradle",
                    "clean",
                    "build",
                ).forwardOutput()
                .withDebug(true)
                .build()

        // Verify the result
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        // Verify that POJOs are generated in the configured directory
        assertThat(File(EXPECTED_DEFAULT_PATH + "Result.java").exists()).isTrue
    }

    @Test
    fun nothingIsGeneratedForNoSchema() {
        // build a project
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project-no-schema-files/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_default_dir.gradle",
                    "clean",
                    "build",
                ).forwardOutput()
                .withDebug(true)
                .build()

        // Verify the result
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        // Check that the generated directory is empty
        assertThat(File(EXPECTED_PATH_EMPTY_SCHEMA).walk().count()).isEqualTo(0)
    }

    @Test
    fun nonGraphQLFilesInSchemaDirectoryAreIgnored() {
        // build a project
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_default_dir.gradle",
                    "clean",
                    "build",
                ).forwardOutput()
                .withDebug(true)
                .build()

        // Verify that the build succeeded.
        // This means there was no parsing error caused by the incorrect syntax in the schema.graphqlconfig file
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        // Verify that a NotSchema POJO has not been created
        // NotSchema is defined in notSchema.notgraphql, which has a non-GraphQL file extension but is a valid schema
        assertThat(File(EXPECTED_DEFAULT_PATH + "NotSchema.java").exists()).isFalse()
    }

    @Test
    fun jacksonVersionOverrideIsApplied() {
        // A valid override (["2", "3"]) should be accepted and supersede classpath detection.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_jackson_override.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
    }

    @Test
    fun invalidJacksonVersionOverrideFailsTheBuild() {
        // An unsupported override value should fail the build with a clear message.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_invalid_jackson_version.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .buildAndFail()

        assertThat(result.output).contains("Invalid 'jacksonVersionOverride' value")
        assertThat(result.output).contains("Invalid Jackson version '4'")
    }

    @Test
    fun detectsJackson3FromCompileClasspath() {
        // End-to-end detection: Jackson 3 is the only Jackson on the compile classpath
        // and the generated Kotlin must use the tools.jackson annotation packages.
        val projectDir = File("src/test/resources/test-project/")
        val result =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_jackson3.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)

        val generatedTypes =
            File(projectDir, "build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types")
                .walk()
                .filter { it.extension == "kt" }
                .joinToString("\n") { it.readText() }

        assertThat(generatedTypes).contains("tools.jackson.databind")
        assertThat(generatedTypes).doesNotContain("com.fasterxml.jackson.databind.`annotation`.JsonDeserialize")
    }

    @Test
    fun generateJavaIsConfigurationCacheCompatible() {
        // generateKotlinNullableClasses is enabled so Jackson version detection (the lazy
        // rootComponent classpath walk) actually runs under the configuration cache.
        assertConfigurationCacheRoundTrip(
            buildFile = "build_with_nullable_classes.gradle",
        )
    }

    @Test
    fun schemaPathsAcceptsAProvider() {
        // setSchemaPaths(Provider<out Iterable<Any>>) must resolve lazily and still produce sources.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_schema_paths_provider.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(EXPECTED_PATH + "Result.java").exists()).isTrue
    }

    @Test
    fun schemaPathsAcceptsAFileCollection() {
        // The fixture asserts that its Configuration is still unresolved after configuration.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_schema_paths_filecollection.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(EXPECTED_PATH + "Result.java").exists()).isTrue
    }

    @Test
    fun schemaPathsFlattensNestedFileSets() {
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments("-PtestBuildFile=build_with_nested_schema_paths.gradle", "clean", "generateJava")
                .build()

        assertThat(result.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(File(EXPECTED_PATH + "Result.java")).exists()
    }

    @Test
    fun getSchemaPathsReturnsAMutableListForBinaryCompatibility() {
        // Preserve the JVM getter descriptor and the mutable-list API used by downstream plugins.
        assertThat(GenerateJavaTask::class.java.getMethod("getSchemaPaths").returnType).isEqualTo(List::class.java)

        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestBuildFile=build_with_schema_paths_addall.gradle",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(EXPECTED_PATH + "Result.java")).exists()
        assertThat(File(EXPECTED_PATH + "AppendedViaAddAll.java")).exists()
    }

    @Test
    fun schemaPathsFileCollectionIsConfigurationCacheCompatible() {
        assertConfigurationCacheRoundTrip("build_with_schema_paths_filecollection.gradle")
    }

    @Test
    fun schemaPathsProviderIsConfigurationCacheCompatible() {
        assertConfigurationCacheRoundTrip("build_with_schema_paths_provider.gradle")
    }

    private fun assertConfigurationCacheRoundTrip(buildFile: String) {
        val projectDir = File("src/test/resources/test-project/")

        fun run() =
            GradleRunner
                .create()
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "--configuration-cache",
                    "--configuration-cache-problems=fail",
                    "-PtestBuildFile=$buildFile",
                    "clean",
                    "generateJava",
                ).forwardOutput()
                .build()

        File(projectDir, ".gradle/configuration-cache").deleteRecursively()

        val first = run()
        assertThat(first.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(first.output).contains("Configuration cache entry stored.")
        assertThat(File(EXPECTED_PATH + "Result.java")).exists()

        val second = run()
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(File(EXPECTED_PATH + "Result.java")).exists()
    }

    @Test
    fun generateJavaIsUpToDateWhenSchemaIsUnchanged(
        @TempDir tempDir: File,
    ) {
        // Keep verifying the content-based task avoidance provided by
        // @InputFiles after changing how schemaPaths accepts and resolves inputs.
        val sourceDir = File("src/test/resources/test-project")
        File(sourceDir, "build.gradle").copyTo(File(tempDir, "build.gradle"))
        File(sourceDir, "settings.gradle").copyTo(File(tempDir, "settings.gradle"))
        File(sourceDir, "src").copyRecursively(File(tempDir, "src"))

        fun run(vararg tasks: String) =
            GradleRunner
                .create()
                .withProjectDir(tempDir)
                .withPluginClasspath()
                .withArguments("--stacktrace", *tasks)
                .forwardOutput()
                .build()

        run("generateJava")
        assertThat(run("generateJava").task(":generateJava")).extracting { it?.outcome }.isEqualTo(UP_TO_DATE)

        // Modifying a schema file must invalidate the cached input snapshot.
        File(tempDir, "src/main/resources/schema/schema.graphqls").appendText("\n# touch\n")

        assertThat(run("generateJava").task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
    }

    @Test
    fun schemaPathsResolvesRelativeStringsAgainstTheOwningProjectDirectory() {
        // Gradle resolves a relative String against the subproject that owns the task.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project-multimodule/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "clean",
                    ":server:generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":server:generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(
            File(
                "src/test/resources/test-project-multimodule/server/build/graphql/generated/sources/dgs-codegen/" +
                    "com/netflix/testproject/graphql/types/Result.java",
            ).exists(),
        ).isTrue
    }

    @Test
    fun schemaPathsResolvesRelativeFilesAgainstTheOwningProjectDirectory() {
        // Gradle resolves a relative File against the subproject that owns the task.
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project-multimodule/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-PtestServerBuildFile=build_with_relative_file.gradle",
                    "clean",
                    ":server:generateJava",
                ).forwardOutput()
                .build()

        assertThat(result.task(":server:generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(
            File(
                "src/test/resources/test-project-multimodule/server/build/graphql/generated/sources/dgs-codegen/" +
                    "com/netflix/testproject/graphql/types/Result.java",
            ).exists(),
        ).isTrue
    }

    companion object {
        const val EXPECTED_PATH =
            "src/test/resources/test-project/build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
        const val EXPECTED_DEFAULT_PATH =
            "src/test/resources/test-project/build/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
        const val EXPECTED_PATH_EMPTY_SCHEMA =
            "src/test/resources/test-project-no-schema-files/build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
    }
}
