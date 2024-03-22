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
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.nio.file.Paths

class CodeGenCli : CliktCommand("Generate Java sources for SCHEMA file(s)") {

    private val schemas by argument().file(mustExist = true).multiple()
    private val output by option("--output-dir", "-o", help = "Output directory").file(
        canBeFile = false,
        canBeDir = true
    ).default(File("generated"))
    private val packageName by option("--package-name", "-p", help = "Package name for generated types")
    private val subPackageNameClient by option("--sub-package-name-client", help = "Sub package name for generated client").default("client")
    private val subPackageNameDatafetchers by option("--sub-package-name-datafetchers", help = "Sub package name for generated datafetchers").default("datafetchers")
    private val subPackageNameTypes by option("--sub-package-name-types", help = "Sub package name for generated types").default("types")
    private val generateBoxedTypes by option("--generate-boxed-types", "-b", help = "Genereate boxed types").flag(default = false)
    private val writeFiles by option("--write-to-disk", "-w", help = "Write files to disk").flag(
        "--console-output",
        default = true
    )
    private val language by option("--language", "-l", help = "Output language").choice("java", "kotlin", ignoreCase = true)
        .default("java")
    private val generateClient by option("--generate-client", "-c", help = "Generate client api").flag(default = false)
    private val generateDataTypes by option(
        "--generate-data-types",
        help = "Generate data types. Not needed when only generating an API"
    ).flag("--skip-generate-data-types", default = true)
    private val generateInterfaces by option("--generate-interfaces", "-i", help = "Generate interfaces for data types").flag(default = false)
    private val includeQueries by option("--include-query").multiple().unique()
    private val includeMutations by option("--include-mutation").multiple().unique()
    private val skipEntityQueries by option("--skip-entities").flag()
    private val typeMapping: Map<String, String> by option("--type-mapping").associate()
    private val shortProjectionNames by option("--short-projection-names").flag()
    private val generateInterfaceSetters by option("--generate-interface-setters").flag()
    private val generateDocs by option("--generate-docs").flag()

    // Generate an additional bitset field and supporting getters, setters, builder functions for data classes
    private val generateFieldIsSet by option("--generate-field-is-set", help = "Generate an additional bitset field and supporting getters, setters, and builder functions for data classes").flag(default = false)

    override fun run() {
        val inputSchemas = if (schemas.isEmpty()) {
            val defaultSchemaPath = Paths.get("src", "main", "resources", "schema")
            if (defaultSchemaPath.toFile().exists()) {
                echo("No schema files or directories specified, defaulting to src/main/resources/schema")
                setOf(defaultSchemaPath.toFile())
            } else {
                throw UsageError("No schema file(s) specified")
            }
        } else {
            schemas.toSet()
        }

        val generate = CodeGen(
            if (packageName != null) {
                CodeGenConfig(
                    schemaFiles = inputSchemas,
                    writeToFiles = writeFiles,
                    outputDir = output.toPath(),
                    packageName = packageName!!,
                    subPackageNameClient = subPackageNameClient,
                    subPackageNameDatafetchers = subPackageNameDatafetchers,
                    subPackageNameTypes = subPackageNameTypes,
                    language = Language.valueOf(language.uppercase()),
                    generateBoxedTypes = generateBoxedTypes,
                    generateClientApi = generateClient,
                    includeQueries = includeQueries,
                    includeMutations = includeMutations,
                    skipEntityQueries = skipEntityQueries,
                    typeMapping = typeMapping,
                    shortProjectionNames = shortProjectionNames,
                    generateDataTypes = generateDataTypes,
                    generateInterfaces = generateInterfaces,
                    generateInterfaceSetters = generateInterfaceSetters,
                    generateDocs = generateDocs,
                    generateFieldIsSet = generateFieldIsSet
                )
            } else {
                CodeGenConfig(
                    schemaFiles = inputSchemas,
                    writeToFiles = writeFiles,
                    outputDir = output.toPath(),
                    subPackageNameClient = subPackageNameClient,
                    subPackageNameDatafetchers = subPackageNameDatafetchers,
                    subPackageNameTypes = subPackageNameTypes,
                    language = Language.valueOf(language.uppercase()),
                    generateBoxedTypes = generateBoxedTypes,
                    generateClientApi = generateClient,
                    includeQueries = includeQueries,
                    includeMutations = includeMutations,
                    skipEntityQueries = skipEntityQueries,
                    typeMapping = typeMapping,
                    shortProjectionNames = shortProjectionNames,
                    generateDataTypes = generateDataTypes,
                    generateInterfaces = generateInterfaces,
                    generateInterfaceSetters = generateInterfaceSetters,
                    generateDocs = generateDocs,
                    generateFieldIsSet = generateFieldIsSet
                )
            }
        ).generate()

        if (writeFiles) {
            echo("${(generate.javaDataTypes + generate.javaInterfaces + generate.javaEnumTypes + generate.javaQueryTypes + generate.clientProjections + generate.javaConstants).size} files written to ${output.absoluteFile}")
        } else {
            echo(generate)
        }
    }
}

fun main(args: Array<String>) {
    CodeGenCli().main(args)
}
