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

@ExperimentalStdlibApi
open class GenerateJavaTask : DefaultTask() {

    private val LOGGER = LoggerFactory.getLogger("DgsCodegenPlugin")

    @Input
    var generatedSourcesDir: String = project.buildDir.absolutePath

    @InputFiles
    var schemaPaths = mutableListOf<Any>("${project.projectDir}/src/main/resources/schema")

    @Input
    var packageName = ""

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
    var generateClient = false

    @OutputDirectory
    fun getOutputDir(): File {
        return Paths.get("${generatedSourcesDir}/generated").toFile()
    }

    @OutputDirectory
    fun getExampleOutputDir(): File {
        return Paths.get("${generatedSourcesDir}/generated-examples").toFile()
    }

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
                language = Language.valueOf(language.toUpperCase()),
                generateClientApi = generateClient,
                typeMapping = typeMapping)

        LOGGER.info("Codegen config: {}", config)

        val codegen = CodeGen(config)
        codegen.generate()
    }
}
