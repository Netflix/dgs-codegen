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

import com.netflix.graphql.dgs.codegen.generators.java.*
import com.netflix.graphql.dgs.codegen.generators.kotlin.*
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findEnumExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findUnionExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import graphql.language.*
import graphql.parser.Parser
import graphql.parser.ParserOptions
import java.io.File
import java.lang.Integer.MAX_VALUE
import java.nio.file.Path
import java.nio.file.Paths

class CodeGen(private val config: CodeGenConfig) {
    lateinit var document: Document
    private lateinit var requiredTypeCollector: RequiredTypeCollector

    @Suppress("DuplicatedCode")
    fun generate(): CodeGenResult {
        if (config.writeToFiles) {
            config.outputDir.toFile().deleteRecursively()
        }

        val inputSchemas = config.schemaFiles.sorted().asSequence()
            .flatMap { it.walkTopDown().filter { file -> file.isFile } }
            .map { it.readText() }
            .plus(config.schemas)
            .toList()

        val joinedSchema = inputSchemas.joinToString("\n")
        val codeGenResult =
            if (config.language == Language.JAVA) generateForSchema(joinedSchema)
            else generateKotlinForSchema(joinedSchema)

        if (config.writeToFiles) {
            codeGenResult.javaDataTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.javaInterfaces.forEach { it.writeTo(config.outputDir) }
            codeGenResult.javaEnumTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.javaDataFetchers.forEach { it.writeTo(config.examplesOutputDir) }
            codeGenResult.javaQueryTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.clientProjections.forEach {
                try {
                    it.writeTo(config.outputDir)
                } catch (ex: Exception) {
                    println(ex.message)
                }
            }
            codeGenResult.javaConstants.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinDataTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinInterfaces.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinEnumTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinDataFetchers.forEach { it.writeTo(config.examplesOutputDir) }
            codeGenResult.kotlinConstants.forEach { it.writeTo(config.outputDir) }
        }

        return codeGenResult
    }

    private fun generateForSchema(schema: String): CodeGenResult {
        val SDL_MAX_ALLOWED_SCHEMA_TOKENS: Int = Int.MAX_VALUE
        val parserOptions = ParserOptions.getDefaultParserOptions()
            .transform {
                it.maxTokens(SDL_MAX_ALLOWED_SCHEMA_TOKENS)
            }
        ParserOptions.setDefaultParserOptions(parserOptions)
        document = Parser.parse(schema)
        requiredTypeCollector = RequiredTypeCollector(
            document,
            queries = config.includeQueries,
            mutations = config.includeMutations,
            subscriptions = config.includeSubscriptions
        )

        val definitions = document.definitions
        // data types
        val dataTypesResult = generateJavaDataType(definitions)
        val inputTypesResult = generateJavaInputType(definitions)
        val interfacesResult = generateJavaInterfaces(definitions)
        val unionsResult = generateJavaUnions(definitions)
        val enumsResult = generateJavaEnums(definitions)
        val constantsClass = ConstantsGenerator(config, document).generate()
        // Client
        val client = generateJavaClientApi(definitions)
        val entitiesClient = generateJavaClientEntitiesApi(definitions)
        val entitiesRepresentationsTypes = generateJavaClientEntitiesRepresentations(definitions)
        // Data Fetchers
        val dataFetchersResult = generateJavaDataFetchers(definitions)

        return dataTypesResult
            .merge(dataFetchersResult)
            .merge(inputTypesResult)
            .merge(unionsResult)
            .merge(enumsResult)
            .merge(interfacesResult)
            .merge(client)
            .merge(entitiesClient)
            .merge(entitiesRepresentationsTypes)
            .merge(constantsClass)
    }

    private fun generateJavaEnums(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<EnumTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || config.generateInterfaces || it.name in requiredTypeCollector.requiredTypes }
            .map { EnumTypeGenerator(config).generate(it, findEnumExtensions(it.name, definitions)) }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateJavaUnions(definitions: Collection<Definition<*>>): CodeGenResult {
        if (!config.generateDataTypes) {
            return CodeGenResult()
        }
        return definitions.asSequence()
            .filterIsInstance<UnionTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map { UnionTypeGenerator(config).generate(it, findUnionExtensions(it.name, definitions)) }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateJavaInterfaces(definitions: Collection<Definition<*>>): CodeGenResult {
        if (!config.generateDataTypes && !config.generateInterfaces) {
            return CodeGenResult()
        }

        return definitions.asSequence()
            .filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val extensions = findInterfaceExtensions(it.name, definitions)
                InterfaceGenerator(config, document).generate(it, extensions)
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateJavaClientApi(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi) {
            definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { OperationTypes.isOperationType(it.name) }
                .map { ClientApiGenerator(config, document).generate(it) }
                .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        } else CodeGenResult()
    }

    private fun generateJavaClientEntitiesApi(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi) {
            val federatedDefinitions = definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.hasDirective("key") }
                .toList()
            ClientApiGenerator(config, document).generateEntities(federatedDefinitions)
        } else CodeGenResult()
    }

    private fun generateJavaClientEntitiesRepresentations(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi) {
            val generatedRepresentations = mutableMapOf<String, Any>()
            return definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.hasDirective("key") }
                .map { d ->
                    EntitiesRepresentationTypeGenerator(config, document).generate(d, generatedRepresentations)
                }.fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        } else CodeGenResult()
    }

    private fun generateJavaDataFetchers(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateDataFetchersAsInterfaces) {
            definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .map { DataFetcherInterfaceGenerator(config, document).generate(it) }
                .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        } else {
            definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.name == OperationTypes.query }
                .map { DatafetcherGenerator(config, document).generate(it) }
                .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        }
    }

    private fun generateJavaDataType(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { it.name != OperationTypes.query && it.name != OperationTypes.mutation && it.name != "RelayPageInfo" }
            .filter { config.generateInterfaces || config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                DataTypeGenerator(config, document).generate(it, findTypeExtensions(it.name, definitions))
            }.fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateJavaInputType(definitions: Collection<Definition<*>>): CodeGenResult {
        val inputTypes = definitions.asSequence()
            .filterIsInstance<InputObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }

        return inputTypes
            .map { d ->
                InputTypeGenerator(config, document).generate(d, findInputExtensions(d.name, definitions))
            }.fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateKotlinForSchema(schema: String): CodeGenResult {
        val options = ParserOptions
            .getDefaultParserOptions()
            .transform { o -> o.maxTokens(MAX_VALUE) }
        val parser = Parser()
        document = parser.parseDocument(schema, options)
        requiredTypeCollector = RequiredTypeCollector(
            document,
            queries = config.includeQueries,
            mutations = config.includeMutations,
            subscriptions = config.includeSubscriptions,
        )

        val definitions = document.definitions

        val datatypesResult = generateKotlinDataTypes(definitions)
        val inputTypes = generateKotlinInputTypes(definitions)
        val interfacesResult = generateKotlinInterfaceTypes(definitions)

        val unionResult = definitions.asSequence()
            .filterIsInstance<UnionTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val extensions = findUnionExtensions(it.name, definitions)
                KotlinUnionTypeGenerator(config).generate(it, extensions)
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }

        val enumsResult = definitions.asSequence()
            .filterIsInstance<EnumTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                val extensions = findEnumExtensions(it.name, definitions)
                KotlinEnumTypeGenerator(config).generate(it, extensions)
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }

        val dataFetcherResult = if (config.generateDataFetchersAsInterfaces) {
            definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .map { KotlinDataFetcherInterfaceGenerator(config).generate(it) }
                .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        } else {
            CodeGenResult()
        }

        val constantsClass = KotlinConstantsGenerator(config, document).generate()

        val client = generateJavaClientApi(definitions)
        val entitiesClient = generateJavaClientEntitiesApi(definitions)
        val entitiesRepresentationsTypes = generateKotlinClientEntitiesRepresentations(definitions)

        return datatypesResult.merge(inputTypes).merge(interfacesResult).merge(unionResult).merge(enumsResult).merge(dataFetcherResult)
            .merge(client).merge(entitiesClient).merge(entitiesRepresentationsTypes).merge(constantsClass)
    }

    private fun generateKotlinClientEntitiesRepresentations(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi) {
            val generatedRepresentations = mutableMapOf<String, Any>()
            return definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.hasDirective("key") }
                .map { d ->
                    KotlinEntitiesRepresentationTypeGenerator(config, document).generate(d, generatedRepresentations)
                }.fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
        } else CodeGenResult()
    }

    private fun generateKotlinInputTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<InputObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                KotlinInputTypeGenerator(config, document).generate(it, findInputExtensions(it.name, definitions))
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateKotlinDataTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { !OperationTypes.isOperationType(it.name) && it.name != "RelayPageInfo" }
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                val extensions = findTypeExtensions(it.name, definitions)
                KotlinDataTypeGenerator(config, document).generate(it, extensions)
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }

    private fun generateKotlinInterfaceTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        if (!config.generateDataTypes && !config.generateInterfaces) {
            return CodeGenResult()
        }

        return definitions.asSequence()
            .filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val extensions = findInterfaceExtensions(it.name, definitions)
                KotlinInterfaceTypeGenerator(config).generate(it, document, extensions)
            }
            .fold(CodeGenResult()) { t: CodeGenResult, u: CodeGenResult -> t.merge(u) }
    }
}

data class CodeGenConfig(
    val schemas: Set<String> = emptySet(),
    val schemaFiles: Set<File> = emptySet(),
    val outputDir: Path = Paths.get("generated"),
    val examplesOutputDir: Path = Paths.get("generated-examples"),
    val writeToFiles: Boolean = false,
    val packageName: String = "com.netflix.${Paths.get("").toAbsolutePath().fileName}.generated",
    private val subPackageNameClient: String = "client",
    private val subPackageNameDatafetchers: String = "datafetchers",
    private val subPackageNameTypes: String = "types",
    val language: Language = Language.JAVA,
    val generateBoxedTypes: Boolean = false,
    val generateClientApi: Boolean = false,
    val generateInterfaces: Boolean = false,
    val typeMapping: Map<String, String> = emptyMap(),
    val includeQueries: Set<String> = emptySet(),
    val includeMutations: Set<String> = emptySet(),
    val includeSubscriptions: Set<String> = emptySet(),
    val skipEntityQueries: Boolean = false,
    val shortProjectionNames: Boolean = false,
    val generateDataTypes: Boolean = true,
    val omitNullInputFields: Boolean = false,
    val maxProjectionDepth: Int = 10,
    val kotlinAllFieldsOptional: Boolean = false,
    /** If enabled, the names of the classes available via the DgsConstant class will be snake cased.*/
    val snakeCaseConstantNames: Boolean = false,
    val generateInterfaceSetters: Boolean = true,
    val generateDataFetchersAsInterfaces: Boolean = false,
) {
    val packageNameClient: String
        get() = "$packageName.$subPackageNameClient"

    val packageNameDatafetchers: String
        get() = "$packageName.$subPackageNameDatafetchers"

    val packageNameTypes: String
        get() = "$packageName.$subPackageNameTypes"

    override fun toString(): String {
        return """
            --output-dir=$outputDir
            --package-name=$packageName
            --sub-package-name-client=$subPackageNameClient
            --sub-package-name-datafetchers=$subPackageNameDatafetchers
            --sub-package-name-types=$subPackageNameTypes
            ${if (generateBoxedTypes) "--generate-boxed-types" else ""}
            ${if (writeToFiles) "--write-to-disk" else ""}
            --language=$language
            ${if (generateClientApi) "--generate-client" else ""}
            ${if (generateDataTypes) "--generate-data-types" else "--skip-generate-data-types"}
            ${includeQueries.joinToString("\n") { "--include-query=$it" }}
            ${includeMutations.joinToString("\n") { "--include-mutation=$it" }}
            ${if (skipEntityQueries) "--skip-entities" else ""}
            ${typeMapping.map { "--type-mapping ${it.key}=${it.value}" }.joinToString("\n")}           
            ${if (shortProjectionNames) "--short-projection-names" else ""}
            ${schemas.joinToString(" ")}
        """.trimIndent()
    }
}

enum class Language {
    JAVA,
    KOTLIN
}

data class CodeGenResult(
    val javaDataTypes: List<JavaFile> = listOf(),
    val javaInterfaces: List<JavaFile> = listOf(),
    val javaEnumTypes: List<JavaFile> = listOf(),
    val javaDataFetchers: List<JavaFile> = listOf(),
    val javaQueryTypes: List<JavaFile> = listOf(),
    val clientProjections: List<JavaFile> = listOf(),
    val javaConstants: List<JavaFile> = listOf(),
    val kotlinDataTypes: List<FileSpec> = listOf(),
    val kotlinInterfaces: List<FileSpec> = listOf(),
    val kotlinEnumTypes: List<FileSpec> = listOf(),
    val kotlinDataFetchers: List<FileSpec> = listOf(),
    val kotlinConstants: List<FileSpec> = emptyList()
) {
    fun merge(current: CodeGenResult): CodeGenResult {
        val javaDataTypes = this.javaDataTypes.plus(current.javaDataTypes)
        val javaInterfaces = this.javaInterfaces.plus(current.javaInterfaces)
        val javaEnumTypes = this.javaEnumTypes.plus(current.javaEnumTypes)
        val javaDataFetchers = this.javaDataFetchers.plus(current.javaDataFetchers)
        val javaQueryTypes = this.javaQueryTypes.plus(current.javaQueryTypes)
        val clientProjections = this.clientProjections.plus(current.clientProjections)
        val javaConstants = this.javaConstants.plus(current.javaConstants)
        val kotlinDataTypes = this.kotlinDataTypes.plus(current.kotlinDataTypes)
        val kotlinInterfaces = this.kotlinInterfaces.plus(current.kotlinInterfaces)
        val kotlinEnumTypes = this.kotlinEnumTypes.plus(current.kotlinEnumTypes)
        val kotlinDataFetchers = this.kotlinDataFetchers.plus(current.kotlinDataFetchers)
        val kotlinConstants = this.kotlinConstants.plus(current.kotlinConstants)

        return CodeGenResult(
            javaDataTypes = javaDataTypes,
            javaInterfaces = javaInterfaces,
            javaEnumTypes = javaEnumTypes,
            javaDataFetchers = javaDataFetchers,
            javaQueryTypes = javaQueryTypes,
            clientProjections = clientProjections,
            javaConstants = javaConstants,
            kotlinDataTypes = kotlinDataTypes,
            kotlinInterfaces = kotlinInterfaces,
            kotlinEnumTypes = kotlinEnumTypes,
            kotlinDataFetchers = kotlinDataFetchers,
            kotlinConstants = kotlinConstants
        )
    }

    fun javaSources(): List<JavaFile> {
        return javaDataTypes
            .asSequence()
            .plus(javaInterfaces)
            .plus(javaEnumTypes)
            .plus(javaDataFetchers)
            .plus(javaQueryTypes)
            .plus(clientProjections)
            .plus(javaConstants)
            .toList()
    }

    fun kotlinSources(): List<FileSpec> {
        return kotlinDataTypes
            .asSequence()
            .plus(kotlinInterfaces)
            .plus(kotlinEnumTypes)
            .plus(kotlinConstants)
            .toList()
    }
}

fun List<FieldDefinition>.filterSkipped(): List<FieldDefinition> {
    return this.filter { it.directives.none { d -> d.name == "skipcodegen" } }
}

fun List<FieldDefinition>.filterIncludedInConfig(definitionName: String, config: CodeGenConfig): List<FieldDefinition> {
    return when (definitionName) {
        OperationTypes.query -> {
            if (config.includeQueries.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeQueries }
            }
        }
        OperationTypes.mutation -> {
            if (config.includeMutations.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeMutations }
            }
        }
        OperationTypes.subscription -> {
            if (config.includeSubscriptions.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeSubscriptions }
            }
        }
        else -> this
    }
}

fun ObjectTypeDefinition.shouldSkip(config: CodeGenConfig): Boolean = shouldSkip(this, config)

fun InputObjectTypeDefinition.shouldSkip(config: CodeGenConfig): Boolean = shouldSkip(this, config)

fun InterfaceTypeDefinition.shouldSkip(config: CodeGenConfig): Boolean = shouldSkip(this, config)

fun UnionTypeDefinition.shouldSkip(config: CodeGenConfig): Boolean = shouldSkip(this, config)

fun EnumTypeDefinition.shouldSkip(config: CodeGenConfig): Boolean = shouldSkip(this, config)

private fun <T : DirectivesContainer<*>> shouldSkip(
    typeDefinition: DirectivesContainer<T>,
    config: CodeGenConfig
): Boolean {
    return typeDefinition.directives.any { it.name == "skipcodegen" } || config.typeMapping.containsKey((typeDefinition as NamedNode<*>).name)
}

fun TypeDefinition<*>.fieldDefinitions(): List<FieldDefinition> {
    return when (this) {
        is ObjectTypeDefinition -> this.fieldDefinitions
        is InterfaceTypeDefinition -> this.fieldDefinitions
        else -> emptyList()
    }
}

fun Type<*>.findTypeDefinition(
    document: Document,
    excludeExtensions: Boolean = false,
    includeBaseTypes: Boolean = false,
    includeScalarTypes: Boolean = false
): TypeDefinition<*>? {
    return when (this) {
        is NonNullType -> {
            this.type.findTypeDefinition(document, excludeExtensions, includeBaseTypes, includeScalarTypes)
        }
        is ListType -> {
            this.type.findTypeDefinition(document, excludeExtensions, includeBaseTypes, includeScalarTypes)
        }
        else -> {
            if (includeBaseTypes && this.isBaseType()) {
                this.findBaseTypeDefinition()
            } else {
                document.definitions.asSequence().filterIsInstance<TypeDefinition<*>>().find {
                    if (it is ScalarTypeDefinition) {
                        includeScalarTypes && it.name == (this as TypeName).name
                    } else {
                        it.name == (this as TypeName).name && (!excludeExtensions || it !is ObjectTypeExtensionDefinition)
                    }
                }
            }
        }
    }
}

fun Type<*>.isBaseType(): Boolean {
    return when (this) {
        is NonNullType -> {
            this.type.isBaseType()
        }
        is ListType -> {
            this.type.isBaseType()
        }
        is TypeName -> {
            when (this.name) {
                "String", "Boolean", "Float", "Int" -> true
                else -> false
            }
        }
        else -> {
            false
        }
    }
}

fun Type<*>.findBaseTypeDefinition(): TypeDefinition<*>? {
    return when (this) {
        is NonNullType -> {
            this.type.findBaseTypeDefinition()
        }
        is ListType -> {
            this.type.findBaseTypeDefinition()
        }
        is TypeName -> {
            when (this.name) {
                "String" -> ScalarTypeDefinition.newScalarTypeDefinition().name("String").build()
                "Boolean" -> ScalarTypeDefinition.newScalarTypeDefinition().name("Boolean").build()
                "Float" -> ScalarTypeDefinition.newScalarTypeDefinition().name("Float").build()
                "Int" -> ScalarTypeDefinition.newScalarTypeDefinition().name("Int").build()
                else -> null
            }
        }
        else -> {
            null
        }
    }
}
