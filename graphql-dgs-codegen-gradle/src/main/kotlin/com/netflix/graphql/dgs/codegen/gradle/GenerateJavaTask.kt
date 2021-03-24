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

import com.netflix.graphql.dgs.codegen.CodeGen
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.Language
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

open class GenerateJavaTask : DefaultTask() {

    private val LOGGER = LoggerFactory.getLogger("DgsCodegenPlugin")

    @Input
    var generatedSourcesDir: String = project.buildDir.absolutePath

    @InputFiles
    var schemaPaths = mutableListOf<Any>("${project.projectDir}/src/main/resources/schema")

    @Input
    var packageName = "com.netflix.dgs.codgen.generated"

    @Input
    var subPackageNameClient = "client"

    @Input
    var subPackageNameDatafetchers = "datafetchers"

    @Input
    var subPackageNameTypes = "types"

    private val hasKotlinPluginWrapperClass = try {
        this.javaClass.classLoader.loadClass("org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper")
        true
    } catch (ex: Exception) {
        false
    }

    @Input
    var language = if (hasKotlinPluginWrapperClass && project.plugins.hasPlugin(KotlinPluginWrapper::class.java)) "KOTLIN" else "JAVA"

    @Input
    var typeMapping = mutableMapOf<String, String>()

    @Input
    var generateBoxedTypes = false

    @Input
    var generateClient = false

    @Input
    var generateDataTypes = true

    @Input
    var generateInterfaces = false

    @OutputDirectory
    fun getOutputDir(): File {
        return Paths.get("$generatedSourcesDir/generated").toFile()
    }

    @OutputDirectory
    fun getExampleOutputDir(): File {
        return Paths.get("$generatedSourcesDir/generated-examples").toFile()
    }

    @Input
    var includeQueries = mutableListOf<String>()

    @Input
    var includeMutations = mutableListOf<String>()

    @Input
    var skipEntityQueries = false

    @Input
    var shortProjectionNames = false

    @Input
    var omitNullInputFields = false

    @Input
    var maxProjectionDepth = 10

    @TaskAction
    fun generate() {
        val schemaPaths = schemaPaths.map { Paths.get(it.toString()).toFile() }.toSet()
        schemaPaths.filter { !it.exists() }.forEach {
            LOGGER.warn("Schema location ${it.absolutePath} does not exist")
        }

        val config = CodeGenConfig(
            schemas = emptySet(),
            schemaFiles = schemaPaths,
            outputDir = getOutputDir().toPath(),
            examplesOutputDir = getExampleOutputDir().toPath(),
            writeToFiles = true,
            packageName = packageName,
            subPackageNameClient = subPackageNameClient,
            subPackageNameDatafetchers = subPackageNameDatafetchers,
            subPackageNameTypes = subPackageNameTypes,
            language = Language.valueOf(language.toUpperCase()),
            generateBoxedTypes = generateBoxedTypes,
            generateClientApi = generateClient,
            generateInterfaces = generateInterfaces,
            typeMapping = typeMapping,
            includeQueries = includeQueries.toSet(),
            includeMutations = includeMutations.toSet(),
            skipEntityQueries = skipEntityQueries,
            shortProjectionNames = shortProjectionNames,
            generateDataTypes = generateDataTypes,
            omitNullInputFields = omitNullInputFields,
            maxProjectionDepth = maxProjectionDepth,
        )

        LOGGER.info("Codegen config: {}", config)

        val codegen = CodeGen(config)
        codegen.generate()
    }
}
