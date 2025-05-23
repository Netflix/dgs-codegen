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

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import java.io.File

/**
 * A simple unit test for the 'com.netflix.graphql.dgs.greeting' plugin.
 */
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
                    "-c",
                    "smoke_test_settings.gradle",
                    "tasks",
                    "--all",
                ).forwardOutput()
                .build()

        // Verify the result
        assertThat(result.output).contains("generateJava")
    }

    @Test
    fun taskDependenciesRegisteredSuccessfully() {
        // get a list of Gradle tasks
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "-c",
                    "smoke_test_settings.gradle",
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
                    "-c",
                    "smoke_test_settings.gradle",
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
                    "-c",
                    "smoke_test_settings_with_default_dir.gradle",
                    "-b",
                    "build_with_default_dir.gradle",
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
    fun sourcesGenerated_OmitNullInputFields() {
        // build a project
        val result =
            GradleRunner
                .create()
                .withProjectDir(File("src/test/resources/test-project/"))
                .withPluginClasspath()
                .withArguments(
                    "--stacktrace",
                    "-c",
                    "smoke_test_settings_omit_null_input_fields.gradle",
                    "-b",
                    "build_omit_null_input_fields.gradle",
                    "clean",
                    "build",
                ).forwardOutput()
                .withDebug(true)
                .build()

        // Verify the result
        assertThat(result.task(":build")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        // Verify that POJOs are generated in the configured directory
        assertThat(File(EXPECTED_DEFAULT_PATH + "Result.java").exists()).isTrue
        assertThat(File(EXPECTED_DEFAULT_PATH + "Filter.java").exists()).isTrue
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
                    "-c",
                    "smoke_test_settings_with_default_dir.gradle",
                    "-b",
                    "build_with_default_dir.gradle",
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
                    "-c",
                    "smoke_test_settings_with_default_dir.gradle",
                    "-b",
                    "build_with_default_dir.gradle",
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

    companion object {
        const val EXPECTED_PATH =
            "src/test/resources/test-project/build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
        const val EXPECTED_DEFAULT_PATH =
            "src/test/resources/test-project/build/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
        const val EXPECTED_PATH_EMPTY_SCHEMA =
            "src/test/resources/test-project-no-schema-files/build/graphql/generated/sources/dgs-codegen/com/netflix/testproject/graphql/types/"
    }
}
