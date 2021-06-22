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

import com.netflix.graphql.dgs.codegen.gradle.ClientUtilsConventions
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CodegenGradlePluginClientUtilsConventionsTest {

    @TempDir
    lateinit var projectDir: File

    private val inferredVersion = ClientUtilsConventions.pluginMetaInfVersion

    @Test
    fun `If disabled, will not add the graphql-dgs-codegen-client-core`() {
        prepareBuildGradleFile(
            """
plugins {
    id 'java'
    id 'com.netflix.dgs.codegen'
}

repositories { mavenCentral() }

dependencies { }

codegen.clientCoreConventionsEnabled = false
""".trimMargin()
        )

        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withPluginClasspath(emptyList())
            .withDebug(true)
            .withArguments("--info", "--stacktrace", "dependencies", "--configuration=compileClasspath")

        val result = runner.build()

        assertThat(result.output)
            .doesNotContain("DGS CodeGen added [com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core")
        assertThat(result.output)
            .doesNotContain("com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core")
    }

    @Test
    fun `Adds the graphql-dgs-codegen-client-core to the classpath`() {
        // given a build file with the plugin.
        prepareBuildGradleFile(
            """
plugins {
    id 'java'
    id 'com.netflix.dgs.codegen'
}

repositories { mavenCentral() }

dependencies { }

""".trimMargin()
        )
        // when the build is executed
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withPluginClasspath(emptyList())
            .withDebug(true)
            .withArguments("--info", "--stacktrace", "dependencies", "--configuration=compileClasspath")

        val result = runner.build()
        // then we assert that the dependency was resolved to the proper version
        assertThat(result.output)
            .contains("DGS CodeGen added [com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:${inferredVersion.get()}")
        assertThat(result.output)
            .contains("- com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:${inferredVersion.get()}")
    }

    @Test
    fun `Can define the specific version for the graphql-dgs-codegen-client-core`() {
        // given a higher version
        val higherVersion = "123456"
        // and a build file that configures the client core version to use such version
        prepareBuildGradleFile(
            """
plugins {
    id 'java'
    id 'com.netflix.dgs.codegen'
}

repositories { mavenCentral() }

dependencies { }

codegen {
    clientCoreVersion = "$higherVersion"
}
""".trimMargin()
        )
        // when the build is executed
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withPluginClasspath(emptyList())
            .withDebug(true)
            .withArguments("--info", "--stacktrace", "dependencies", "--configuration=compileClasspath")

        val result = runner.build()
        // then we assert that the dependency was resolved to the higher version.
        assertThat(result.output)
            .contains("DGS CodeGen added [com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:$higherVersion")
        assertThat(result.output)
            .contains("- com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:$higherVersion FAILED")
    }

    @Test
    fun `Can be overridden by an explicit dependency to graphql-dgs-codegen-client-core`() {
        // given a higher version
        val higherVersion = "123456"
        // and a build file that depends on it.
        prepareBuildGradleFile(
            """
plugins {
    id 'java'
    id 'com.netflix.dgs.codegen'
}

repositories { mavenCentral() }

dependencies { 
    implementation 'com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:$higherVersion'
}
""".trimMargin()
        )
        // when the build is executed
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withPluginClasspath(emptyList())
            .withDebug(true)
            .withArguments("--info", "--stacktrace", "dependencies", "--configuration=compileClasspath")

        val result = runner.build()
        // then we assert that the dependency was resolved to the higher version.
        assertThat(result.output)
            .contains("DGS CodeGen added [com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core")
        assertThat(result.output)
            .contains("- com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core:${inferredVersion.get()} -> $higherVersion FAILED")
    }

    private fun prepareBuildGradleFile(content: String) {
        writeProjectFile("build.gradle", content)
    }

    private fun writeProjectFile(relativePath: String, content: String) {
        val file = File(projectDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}
