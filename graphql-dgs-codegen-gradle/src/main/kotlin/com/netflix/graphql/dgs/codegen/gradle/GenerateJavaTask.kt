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
import org.gradle.api.tasks.*
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.io.File
import java.nio.file.Paths
import java.util.*

open class GenerateJavaTask : DefaultTask() {
    @Input
    var generatedSourcesDir: String = project.buildDir.absolutePath

    @InputFiles
    var schemaPaths = mutableListOf<Any>("${project.projectDir}/src/main/resources/schema")

    @Input
    var outputDir = "$generatedSourcesDir/generated/sources/dgs-codegen"

    @Input
    var packageName = "com.netflix.dgs.codegen.generated"

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
    var generateClientv2 = false

    @Input
    var generateKotlinNullableClasses = false

    @Input
    var generateKotlinClosureProjections = false

    @Input
    var generateDataTypes = true

    @Input
    var generateInterfaces = false

    @Input
    var generateInterfaceSetters = true

    @Input
    var generateInterfaceMethodsForInterfaceFields = false

    @Input
    var implementSerializable = false

    @OutputDirectory
    fun getOutputDir(): File {
        return Paths.get(outputDir).toFile()
    }

    fun setOutputDir() {
        outputs
    }

    @OutputDirectory
    fun getExampleOutputDir(): File {
        return Paths.get("$generatedSourcesDir/generated/sources/dgs-codegen-generated-examples").toFile()
    }

    @Input
    var includeQueries = mutableListOf<String>()

    @Input
    var includeMutations = mutableListOf<String>()

    @Input
    var includeSubscriptions = mutableListOf<String>()

    @Input
    var skipEntityQueries = false

    @Input
    var shortProjectionNames = false

    @Input
    var omitNullInputFields = false

    @Input
    var maxProjectionDepth = 10

    @Input
    var kotlinAllFieldsOptional = false

    @Input
    var snakeCaseConstantNames = false

    @Input
    var addGeneratedAnnotation = false

    @Input
    var addDeprecatedAnnotation = false

    @Input
    var generateCustomAnnotations = false

    @Input
    var includeImports = mutableMapOf<String, String>()

    @Input
    var includeEnumImports = mutableMapOf<String, MutableMap<String, String>>()

    @Input
    var includeClassImports = mutableMapOf<String, MutableMap<String, String>>()

    @TaskAction
    fun generate() {
        val schemaJarFilesFromDependencies = emptyList<File>().toMutableList()
        val dgsCodegenConfig = project.configurations.findByName("dgsCodegen")
        dgsCodegenConfig?.incoming?.dependencies?.forEach { dependency ->
            logger.info("Found DgsCodegen Dependendency: ${dependency.name}")
            val found = dgsCodegenConfig.incoming.artifacts.resolvedArtifacts.get().find { it.id.componentIdentifier.displayName.contains(dependency.group + ":" + dependency.name) }
            if (found != null) {
                logger.info("Found DgsCodegen Artifact: ${found.id.displayName}")
                schemaJarFilesFromDependencies.add(found.file)
            }
        }

        val schemaPaths = schemaPaths.map { Paths.get(it.toString()).toFile() }.sorted().toSet()
        schemaPaths.filter { !it.exists() }.forEach {
            logger.warn("Schema location ${it.absolutePath} does not exist")
        }
        logger.info("Processing schema files:")
        schemaPaths.forEach {
            logger.info("Processing $it")
        }

        val config = CodeGenConfig(
            schemas = emptySet(),
            schemaFiles = schemaPaths,
            schemaJarFilesFromDependencies = schemaJarFilesFromDependencies,
            outputDir = getOutputDir().toPath(),
            examplesOutputDir = getExampleOutputDir().toPath(),
            writeToFiles = true,
            packageName = packageName,
            subPackageNameClient = subPackageNameClient,
            subPackageNameDatafetchers = subPackageNameDatafetchers,
            subPackageNameTypes = subPackageNameTypes,
            language = Language.valueOf(language.uppercase(Locale.getDefault())),
            generateBoxedTypes = generateBoxedTypes,
            generateClientApi = generateClient,
            generateClientApiv2 = generateClientv2,
            generateKotlinNullableClasses = generateKotlinNullableClasses,
            generateKotlinClosureProjections = generateKotlinClosureProjections,
            generateInterfaces = generateInterfaces,
            generateInterfaceSetters = generateInterfaceSetters,
            generateInterfaceMethodsForInterfaceFields = generateInterfaceMethodsForInterfaceFields,
            typeMapping = typeMapping,
            includeQueries = includeQueries.toSet(),
            includeMutations = includeMutations.toSet(),
            includeSubscriptions = includeSubscriptions.toSet(),
            skipEntityQueries = skipEntityQueries,
            shortProjectionNames = shortProjectionNames,
            generateDataTypes = generateDataTypes,
            omitNullInputFields = omitNullInputFields,
            maxProjectionDepth = maxProjectionDepth,
            kotlinAllFieldsOptional = kotlinAllFieldsOptional,
            snakeCaseConstantNames = snakeCaseConstantNames,
            implementSerializable = implementSerializable,
            addGeneratedAnnotation = addGeneratedAnnotation,
            addDeprecatedAnnotation = addDeprecatedAnnotation,
            includeImports = includeImports,
            includeEnumImports = includeEnumImports,
            includeClassImports = includeClassImports,
            generateCustomAnnotations = generateCustomAnnotations
        )

        logger.info("Codegen config: {}", config)

        val codegen = CodeGen(config)
        codegen.generate()
    }
}
