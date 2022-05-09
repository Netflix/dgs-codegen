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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.nio.file.Paths
import java.util.Locale
import javax.inject.Inject

@CacheableTask
abstract class GenerateJavaTask @Inject constructor(
    projectLayout: ProjectLayout,
    providerFactory: ProviderFactory,
    objectFactory: ObjectFactory
) : DefaultTask() {
    @get:Input
    val generatedSourcesDir: Property<String> = objectFactory.property(String::class.java)
        .convention(projectLayout.buildDirectory.map { it.asFile.absolutePath })

    @PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val schemaPaths: ListProperty<Any> = objectFactory.listProperty(Any::class.java)
        .convention(listOf(projectLayout.projectDirectory.dir("src/main/resources/schema").toString()))

    @get:Input
    val packageName: Property<String> = objectFactory.property(String::class.java)
        .convention("com.netflix.dgs.codegen.generated")

    @get:Input
    val subPackageNameClient: Property<String> = objectFactory.property(String::class.java)
        .convention("client")

    @get:Input
    val subPackageNameDatafetchers: Property<String> = objectFactory.property(String::class.java)
        .convention("datafetchers")

    @get:Input
    val subPackageNameTypes: Property<String> = objectFactory.property(String::class.java)
        .convention("types")

    @get:Input
    val language: Property<String> = objectFactory.property(String::class.java)
        .convention(
            providerFactory.provider {
                val hasKotlinPluginWrapperClass = try {
                    this.javaClass.classLoader.loadClass("org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper")
                    true
                } catch (ex: Exception) {
                    false
                }
                if (hasKotlinPluginWrapperClass && project.plugins.hasPlugin(KotlinPluginWrapper::class.java)) "KOTLIN" else "JAVA"
            }
        )

    @get:Input
    val typeMapping: MapProperty<String, String> = objectFactory.mapProperty(String::class.java, String::class.java)

    @get:Input
    val generateBoxedTypes: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val generateClient: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val generateDataTypes: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(true)

    @get:Input
    val generateInterfaces: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val generateInterfaceSetters: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(true)

    @get:OutputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()
        .convention(
            generatedSourcesDir.flatMap { baseDir ->
                projectLayout.dir(providerFactory.provider { project.file("$baseDir/generated/sources/dgs-codegen") })
            }
        )

    @get:OutputDirectory
    val exampleOutputDir: DirectoryProperty = objectFactory.directoryProperty()
        .convention(
            generatedSourcesDir.flatMap { baseDir ->
                projectLayout.dir(providerFactory.provider { project.file("$baseDir/generated/sources/dgs-codegen-generated-examples") })
            }
        )

    @get:Input
    val includeQueries: SetProperty<String> = objectFactory.setProperty(String::class.java)

    @get:Input
    val includeMutations: SetProperty<String> = objectFactory.setProperty(String::class.java)

    @get:Input
    val includeSubscriptions: SetProperty<String> = objectFactory.setProperty(String::class.java)

    @get:Input
    val skipEntityQueries: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val shortProjectionNames: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val omitNullInputFields: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val maxProjectionDepth: Property<Int> = objectFactory.property(Int::class.javaObjectType)
        .convention(10)

    @get:Input
    val kotlinAllFieldsOptional: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @get:Input
    val snakeCaseConstantNames: Property<Boolean> = objectFactory.property(Boolean::class.java)
        .convention(false)

    @TaskAction
    fun generate() {
        val schemaPaths = schemaPaths.get().asSequence()
            .map { Paths.get(it.toString()).toFile() }
            .toSortedSet()
        schemaPaths.asSequence().filter { !it.exists() }.forEach {
            logger.warn("Schema location {} does not exist", it.absolutePath)
        }
        logger.info("Processing schema files:")
        schemaPaths.forEach {
            logger.info("Processing {}", it)
        }

        val config = CodeGenConfig(
            schemas = emptySet(),
            schemaFiles = schemaPaths,
            outputDir = outputDir.get().asFile.toPath(),
            examplesOutputDir = exampleOutputDir.get().asFile.toPath(),
            writeToFiles = true,
            packageName = packageName.get(),
            subPackageNameClient = subPackageNameClient.get(),
            subPackageNameDatafetchers = subPackageNameDatafetchers.get(),
            subPackageNameTypes = subPackageNameTypes.get(),
            language = Language.valueOf(language.get().uppercase(Locale.getDefault())),
            generateBoxedTypes = generateBoxedTypes.get(),
            generateClientApi = generateClient.get(),
            generateInterfaces = generateInterfaces.get(),
            generateInterfaceSetters = generateInterfaceSetters.get(),
            typeMapping = typeMapping.get(),
            includeQueries = includeQueries.get(),
            includeMutations = includeMutations.get(),
            includeSubscriptions = includeSubscriptions.get(),
            skipEntityQueries = skipEntityQueries.get(),
            shortProjectionNames = shortProjectionNames.get(),
            generateDataTypes = generateDataTypes.get(),
            omitNullInputFields = omitNullInputFields.get(),
            maxProjectionDepth = maxProjectionDepth.get(),
            kotlinAllFieldsOptional = kotlinAllFieldsOptional.get(),
            snakeCaseConstantNames = snakeCaseConstantNames.get()
        )

        logger.info("Codegen config: {}", config)

        val codegen = CodeGen(config)
        codegen.generate()
    }
}
