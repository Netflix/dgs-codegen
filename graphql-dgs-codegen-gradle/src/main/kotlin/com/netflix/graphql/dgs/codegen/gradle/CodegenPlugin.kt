package com.netflix.graphql.dgs.codegen.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

@ExperimentalStdlibApi
class CodegenPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val taskProvider = project.tasks.register("generateJava", GenerateJavaTask::class.java)
        project.getTasksByName("compileJava", false).forEach {
            it.dependsOn(taskProvider.get())
        }

        project.getTasksByName("compileKotlin", false).forEach {
            it.dependsOn(taskProvider.get())
        }

        val javaConvention = project.convention.getPlugin(JavaPluginConvention::class.java)
        val sourceSets = javaConvention.sourceSets
        val mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        val outputDir = taskProvider.get().getOutputDir()

        val srcDirs = mainSourceSet.java.srcDirs + outputDir
        mainSourceSet.java.setSrcDirs(srcDirs)
    }
}