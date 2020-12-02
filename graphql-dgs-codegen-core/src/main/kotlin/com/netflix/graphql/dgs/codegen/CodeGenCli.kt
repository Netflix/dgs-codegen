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

package com.netflix.graphql.dgs.codegen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.nio.file.Paths

@ExperimentalStdlibApi
class CodeGenCli : CliktCommand("Generate Java sources for SCHEMA file(s)") {

    private val schemas by argument().file(exists = true).multiple()
    private val output by option("--output-dir", "-o", help = "Output directory").file(fileOkay = false, folderOkay = true).default(File("generated"))
    private val packageName by option("--package-name", "-p", help = "Package name for generated types")
    private val writeFiles by option("--write-to-disk", "-w", help = "Write files to disk").flag("--console-output", default = true)
    private val language by option("--language", "-l", help = "Output language").choice("java", "kotlin").default("java")
    private val generateClient by option("--generate-client", "-c", help = "Genereate client api").flag(default = false)

    override fun run() {
        val inputSchemas = if(schemas.isEmpty()) {
            val defaultSchemaPath = Paths.get("src", "main", "resources", "schema")
            if(defaultSchemaPath.toFile().exists()) {
                echo("No schema files or directories specified, defaulting to src/main/resources/schema")
                setOf(defaultSchemaPath.toFile())
            } else {
                throw UsageError("No schema file(s) specified")
            }
        } else {
            schemas.toSet()
        }

        val generate = CodeGen(
                if(packageName != null) {
                    CodeGenConfig(schemaFiles = inputSchemas, writeToFiles = writeFiles, outputDir = output.toPath(), packageName = packageName!!, language = Language.valueOf(language.toUpperCase()), generateClientApi = generateClient)
                } else {
                    CodeGenConfig(schemaFiles = inputSchemas, writeToFiles = writeFiles, outputDir = output.toPath(), language = Language.valueOf(language.toUpperCase()), generateClientApi = generateClient)
                }
        ).generate()

        if(writeFiles) {
            when(generate) {
                is CodeGenResult -> echo("${(generate.dataTypes + generate.interfaces + generate.enumTypes + generate.queryTypes + generate.clientProjections + generate.constants).size} files written to ${output.absoluteFile}")
                is KotlinCodeGenResult -> echo("${(generate.dataTypes + generate.interfaces + generate.enumTypes + generate.queryTypes + generate.clientProjections + generate.constants).size} files written to ${output.absoluteFile}")
            }
        } else {
            echo(generate)
        }
    }
}


@ExperimentalStdlibApi
fun main(args: Array<String>) {
    CodeGenCli().main(args)
}