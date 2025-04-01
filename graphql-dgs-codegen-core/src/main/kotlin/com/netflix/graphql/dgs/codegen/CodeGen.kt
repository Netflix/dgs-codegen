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
import com.netflix.graphql.dgs.codegen.generators.kotlin2.*
import com.netflix.graphql.dgs.codegen.generators.shared.DocFileSpec
import com.netflix.graphql.dgs.codegen.generators.shared.DocGenerator
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findEnumExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findUnionExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.excludeSchemaTypeExtension
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import graphql.language.*
import graphql.parser.InvalidSyntaxException
import graphql.parser.MultiSourceReader
import graphql.parser.Parser
import graphql.parser.ParserEnvironment
import graphql.parser.ParserOptions
import graphql.schema.idl.ScalarInfo
import graphql.schema.idl.TypeUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.Reader
import java.lang.annotation.RetentionPolicy
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.lang.model.element.Modifier
import com.squareup.kotlinpoet.AnnotationSpec as KAnnotationSpec
import com.squareup.kotlinpoet.ClassName as KClassName
import com.squareup.kotlinpoet.TypeSpec as KTypeSpec

class CodeGen(private val config: CodeGenConfig) {

    companion object {
        private const val SDL_MAX_ALLOWED_SCHEMA_TOKENS: Int = Int.MAX_VALUE
        private const val SDL_MAX_CHARACTERS: Int = Int.MAX_VALUE
        private val logger: Logger = LoggerFactory.getLogger(CodeGen::class.java)
    }

    private val document = buildDocument()
    private val requiredTypeCollector = RequiredTypeCollector(
        document = document,
        config = config
    )

    fun generate(): CodeGenResult {
        loadTypeMappingsFromDependencies()

        val codeGenResult = when (config.language) {
            Language.JAVA -> generateJava()
            Language.KOTLIN -> generateKotlin()
        }

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
            codeGenResult.kotlinInputTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinInterfaces.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinEnumTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinDataFetchers.forEach { it.writeTo(config.examplesOutputDir) }
            codeGenResult.kotlinConstants.forEach { it.writeTo(config.outputDir) }
            codeGenResult.kotlinClientTypes.forEach { it.writeTo(config.outputDir) }
            codeGenResult.docFiles.forEach { it.writeTo(config.generatedDocsFolder) }
        }

        return codeGenResult
    }

    /**
     * Build a [Document] containing the combined schemas from
     * [config].
     */
    private fun buildDocument(): Document {
        val options = ParserOptions.getDefaultParserOptions().transform { builder ->
            builder
                .maxTokens(SDL_MAX_ALLOWED_SCHEMA_TOKENS)
                .maxWhitespaceTokens(SDL_MAX_ALLOWED_SCHEMA_TOKENS)
                .maxCharacters(SDL_MAX_CHARACTERS)
        }
        val parser = Parser()

        val readerBuilder = MultiSourceReader.newMultiSourceReader()
        val debugReaderBuilder = MultiSourceReader.newMultiSourceReader()

        loadSchemaReaders(readerBuilder, debugReaderBuilder)
        // process schema from dependencies
        config.schemaJarFilesFromDependencies.sorted().forEach { file ->
            val zipFile = ZipFile(file)
            for (entry in zipFile.entries()) {
                if (!entry.isDirectory &&
                    (entry.name.endsWith(".graphqls") || entry.name.endsWith(".graphql"))
                ) {
                    logger.info("Generating schema from {}: {}", file.name, entry.name)
                    readerBuilder.reader(zipFile.getInputStream(entry).reader(), "codegen")
                }
            }
        }

        val document = readerBuilder.build().use { reader ->
            try {
                val parserEnv = ParserEnvironment.newParserEnvironment().document(reader).parserOptions(options).build()
                parser.parseDocument(parserEnv)
            } catch (exception: InvalidSyntaxException) {
                // check if the schema is empty
                if (exception.sourcePreview != null && exception.sourcePreview.isBlank()) {
                    logger.warn("Schema is empty")
                    // return an empty document
                    return Document.newDocument().build()
                } else {
                    throw CodeGenSchemaParsingException(debugReaderBuilder.build(), exception)
                }
            }
        }

        return document
    }

    private fun loadTypeMappingsFromDependencies() {
        // process type mappings from dependencies
        config.schemaJarFilesFromDependencies.forEach { file ->
            JarFile(file).use { jarFile ->
                val typeMappingsFile = jarFile.getJarEntry("META-INF/dgs.codegen.typemappings")
                if (typeMappingsFile != null) {
                    jarFile.getInputStream(typeMappingsFile).use { typeMappingInput ->
                        val props = Properties()
                        props.load(typeMappingInput)

                        // Add the new type mappings from dependencies to existing type mappings.
                        // The user provided config overrides mappings from the dependencies.
                        @Suppress("UNCHECKED_CAST")
                        config.typeMapping = (props as Map<String, String>).plus(config.typeMapping)
                    }
                }
            }
        }
    }

    /**
     * Loads the given [MultiSourceReader.Builder] references with the sources that will be used to provide
     * the schema information for the parser.
     */
    private fun loadSchemaReaders(vararg readerBuilders: MultiSourceReader.Builder) {
        readerBuilders.forEach { rb ->
            val schemaFiles = config.schemaFiles.asSequence()
                .flatMap { it.walkTopDown() }
                .filter { it.isFile }
                .filter { it.name.endsWith(".graphql") || it.name.endsWith(".graphqls") }
                .sorted()
            for (schemaFile in schemaFiles) {
                rb.string("\n", "codegen")
                rb.reader(schemaFile.reader(), schemaFile.name)
            }
            for (schema in config.schemas) {
                rb.string(schema, null)
            }
        }
    }

    private fun generateJava(): CodeGenResult {
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
        val generatedAnnotation = generateJavaGeneratedAnnotation(config)
        val docFiles = generateDocFiles(definitions)

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
            .merge(generatedAnnotation)
            .merge(docFiles)
    }

    private fun generateJavaEnums(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<EnumTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || config.generateInterfaces || it.name in requiredTypeCollector.requiredTypes }
            .map { EnumTypeGenerator(config).generate(it, findEnumExtensions(it.name, definitions)) }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateJavaUnions(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<UnionTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || config.generateInterfaces || it.name in requiredTypeCollector.requiredTypes }
            .map { UnionTypeGenerator(config, document).generate(it, findUnionExtensions(it.name, definitions)) }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateJavaInterfaces(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || config.generateInterfaces || it.name in requiredTypeCollector.requiredTypes }
            .map {
                val extensions = findInterfaceExtensions(it.name, definitions)
                InterfaceGenerator(config, document).generate(it, extensions)
            }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateJavaClientApi(definitions: Collection<Definition<*>>): CodeGenResult {
        val methodNames = mutableSetOf<String>()
        return if (config.generateClientApi || config.generateClientApiv2) {
            definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.name == "Query" || it.name == "Mutation" || it.name == "Subscription" }
                .sortedBy { it.name.length }
                .map {
                    ClientApiGenerator(config, document).generate(it, methodNames)
                }
                .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
        } else CodeGenResult.EMPTY
    }

    private fun generateJavaClientEntitiesApi(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi || config.generateClientApiv2) {
            val federatedDefinitions = definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.hasDirective("key") }
                .toList()
            ClientApiGenerator(config, document).generateEntities(federatedDefinitions)
        } else CodeGenResult.EMPTY
    }

    private fun generateJavaClientEntitiesRepresentations(definitions: Collection<Definition<*>>): CodeGenResult {
        return if (config.generateClientApi || config.generateClientApiv2) {
            val generatedRepresentations = mutableMapOf<String, Any>()
            return definitions.asSequence()
                .filterIsInstance<ObjectTypeDefinition>()
                .filter { it.hasDirective("key") }
                .map { d ->
                    EntitiesRepresentationTypeGenerator(config, document).generate(d, generatedRepresentations)
                }.fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
        } else CodeGenResult.EMPTY
    }

    private fun generateJavaDataFetchers(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .filter { it.name == "Query" }
            .map { DatafetcherGenerator(config, document).generate(it) }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateJavaGeneratedAnnotation(config: CodeGenConfig): CodeGenResult {
        return if (config.addGeneratedAnnotation) {
            val retention = AnnotationSpec.builder(java.lang.annotation.Retention::class.java)
                .addMember("value", "\$T.\$L", RetentionPolicy::class.java, RetentionPolicy.CLASS.name)
                .build()
            val generated =
                TypeSpec.annotationBuilder(ClassName.get(config.packageName, "Generated"))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(retention)
                    .build()
            val generatedFile = JavaFile.builder(config.packageName, generated).build()
            CodeGenResult(javaInterfaces = listOf(generatedFile))
        } else {
            CodeGenResult.EMPTY
        }
    }

    private fun generateJavaDataType(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { it.name != "Query" && it.name != "Mutation" && it.name != "RelayPageInfo" }
            .filter { config.generateInterfaces || config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                DataTypeGenerator(config, document).generate(it, findTypeExtensions(it.name, definitions))
            }.fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateJavaInputType(definitions: Collection<Definition<*>>): CodeGenResult {
        val inputTypeDefinitions = definitions
            .filterIsInstance<InputObjectTypeDefinition>()
        val inputTypes = inputTypeDefinitions.asSequence()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }

        return inputTypes
            .map { d ->
                InputTypeGenerator(config, document).generate(d, findInputExtensions(d.name, definitions), inputTypeDefinitions)
            }.fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateKotlin(): CodeGenResult {
        val definitions = document.definitions

        val requiredTypeCollector = RequiredTypeCollector(
            document = document,
            config = config
        )
        val requiredTypes = requiredTypeCollector.requiredTypes

        val dataTypes = if (config.generateKotlinNullableClasses) {
            CodeGenResult(
                kotlinDataTypes = generateKotlin2DataTypes(config, document, requiredTypes),
                kotlinInputTypes = generateKotlin2InputTypes(config, document, requiredTypes),
                kotlinInterfaces = generateKotlin2Interfaces(config, document),
                kotlinEnumTypes = generateKotlin2EnumTypes(config, document, requiredTypes),
                kotlinConstants = KotlinConstantsGenerator(config, document).generate().kotlinConstants
            )
        } else {
            val datatypesResult = generateKotlinDataTypes(definitions)
            val inputTypes = generateKotlinInputTypes(definitions)
            val interfacesResult = generateKotlinInterfaceTypes(definitions)

            val unionResult = definitions.asSequence()
                .filterIsInstance<UnionTypeDefinition>()
                .excludeSchemaTypeExtension()
                .map {
                    val extensions = findUnionExtensions(it.name, definitions)
                    KotlinUnionTypeGenerator(config, document).generate(it, extensions)
                }
                .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }

            val enumsResult = definitions.asSequence()
                .filterIsInstance<EnumTypeDefinition>()
                .excludeSchemaTypeExtension()
                .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
                .map {
                    val extensions = findEnumExtensions(it.name, definitions)
                    KotlinEnumTypeGenerator(config).generate(it, extensions)
                }
                .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }

            val constantsClass = KotlinConstantsGenerator(config, document).generate()

            datatypesResult
                .merge(inputTypes)
                .merge(interfacesResult)
                .merge(unionResult)
                .merge(enumsResult)
                .merge(constantsClass)
        }

        val clientTypes = if (config.generateKotlinClosureProjections) {
            CodeGenResult(
                kotlinClientTypes = generateKotlin2ClientTypes(config, document)
            )
        } else {
            val client = generateJavaClientApi(definitions)
            val entitiesClient = generateJavaClientEntitiesApi(definitions)
            val entitiesRepresentationsTypes = generateJavaClientEntitiesRepresentations(definitions)

            client.merge(entitiesClient).merge(entitiesRepresentationsTypes)
        }

        val generatedAnnotation = generateKotlinGeneratedAnnotation(config)

        return dataTypes.merge(clientTypes)
            .merge(generatedAnnotation)
    }

    private fun generateKotlinGeneratedAnnotation(config: CodeGenConfig): CodeGenResult {
        return if (config.addGeneratedAnnotation) {
            val generated = KTypeSpec.annotationBuilder(KClassName(config.packageName, "Generated"))
                .addModifiers(KModifier.PUBLIC)
                .addAnnotation(
                    KAnnotationSpec
                        .builder(Retention::class)
                        .addMember("value = %T.%L", AnnotationRetention::class, AnnotationRetention.BINARY.name)
                        .build()
                )
                .build()
            val generatedFile =
                FileSpec.builder(config.packageName, "Generated").addType(generated).build()
            CodeGenResult(kotlinInterfaces = listOf(generatedFile))
        } else {
            CodeGenResult.EMPTY
        }
    }

    private fun generateKotlinInputTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        val inputTypeDefinitions = definitions
            .filterIsInstance<InputObjectTypeDefinition>()
        return inputTypeDefinitions.asSequence()
            .excludeSchemaTypeExtension()
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                KotlinInputTypeGenerator(config, document).generate(it, findInputExtensions(it.name, definitions), inputTypeDefinitions)
            }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateKotlinDataTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        return definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .excludeSchemaTypeExtension()
            .filter { it.name != "Query" && it.name != "Mutation" && it.name != "RelayPageInfo" }
            .filter { config.generateDataTypes || it.name in requiredTypeCollector.requiredTypes }
            .map {
                val extensions = findTypeExtensions(it.name, definitions)
                KotlinDataTypeGenerator(config, document).generate(it, extensions)
            }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateKotlinInterfaceTypes(definitions: Collection<Definition<*>>): CodeGenResult {
        if (!config.generateDataTypes && !config.generateInterfaces) {
            return CodeGenResult.EMPTY
        }

        return definitions.asSequence()
            .filterIsInstance<InterfaceTypeDefinition>()
            .excludeSchemaTypeExtension()
            .map {
                val extensions = findInterfaceExtensions(it.name, definitions)
                KotlinInterfaceTypeGenerator(config, document).generate(it, extensions)
            }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }

    private fun generateDocFiles(definitions: Collection<Definition<*>>): CodeGenResult {
        if (!config.generateDocs) {
            return CodeGenResult.EMPTY
        }

        return definitions.asSequence()
            .map {
                DocGenerator(config, document).generate(it)
            }
            .fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
    }
}

class CodeGenConfig(
    var schemas: Set<String> = emptySet(),
    var schemaFiles: Set<File> = emptySet(),
    var schemaJarFilesFromDependencies: List<File> = emptyList(),
    var outputDir: Path = Paths.get("generated"),
    var examplesOutputDir: Path = Paths.get("generated-examples"),
    var writeToFiles: Boolean = false,
    var packageName: String = "com.netflix.${Paths.get("").toAbsolutePath().fileName}.generated",
    private val subPackageNameClient: String = "client",
    private val subPackageNameDatafetchers: String = "datafetchers",
    private val subPackageNameTypes: String = "types",
    private val subPackageNameDocs: String = "docs",
    var language: Language = Language.JAVA,
    var generateBoxedTypes: Boolean = false,
    var generateIsGetterForPrimitiveBooleanFields: Boolean = false,
    var generateClientApi: Boolean = false,
    var generateClientApiv2: Boolean = false,
    var generateInterfaces: Boolean = false,
    var generateKotlinNullableClasses: Boolean = false,
    var generateKotlinClosureProjections: Boolean = false,
    var typeMapping: Map<String, String> = emptyMap(),
    var includeQueries: Set<String> = emptySet(),
    var includeMutations: Set<String> = emptySet(),
    var includeSubscriptions: Set<String> = emptySet(),
    var skipEntityQueries: Boolean = false,
    var shortProjectionNames: Boolean = false,
    var generateDataTypes: Boolean = true,
    var omitNullInputFields: Boolean = false,
    var maxProjectionDepth: Int = 10,
    var kotlinAllFieldsOptional: Boolean = false,
    /** If enabled, the names of the classes available via the DgsConstant class will be snake cased.*/
    var snakeCaseConstantNames: Boolean = false,
    var generateInterfaceSetters: Boolean = true,
    var generateInterfaceMethodsForInterfaceFields: Boolean = false,
    var generateDocs: Boolean = false,
    var generatedDocsFolder: Path = Paths.get("generated-docs"),
    var includeImports: Map<String, String> = emptyMap(),
    var includeEnumImports: Map<String, Map<String, String>> = emptyMap(),
    var includeClassImports: Map<String, Map<String, String>> = emptyMap(),
    var generateCustomAnnotations: Boolean = false,
    var javaGenerateAllConstructor: Boolean = true,
    var implementSerializable: Boolean = false,
    var addGeneratedAnnotation: Boolean = false,
    var disableDatesInGeneratedAnnotation: Boolean = false,
    var addDeprecatedAnnotation: Boolean = false
) {
    val packageNameClient: String = "$packageName.$subPackageNameClient"

    val packageNameDatafetchers: String = "$packageName.$subPackageNameDatafetchers"

    val packageNameTypes: String = "$packageName.$subPackageNameTypes"
    val packageNameDocs: String = "$packageName.$subPackageNameDocs"

    override fun toString(): String {
        return """
            --output-dir=$outputDir
            --package-name=$packageName
            --sub-package-name-client=$subPackageNameClient
            --sub-package-name-datafetchers=$subPackageNameDatafetchers
            --sub-package-name-types=$subPackageNameTypes
            --sub-package-name-docs=$subPackageNameDocs
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
            ${if (addGeneratedAnnotation) "--add-generated-annotation" else ""}
            ${schemas.joinToString(" ")}
        """.trimIndent()
    }
}

enum class Language {
    JAVA,
    KOTLIN,
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
    val kotlinInputTypes: List<FileSpec> = listOf(),
    val kotlinInterfaces: List<FileSpec> = listOf(),
    val kotlinEnumTypes: List<FileSpec> = listOf(),
    val kotlinDataFetchers: List<FileSpec> = listOf(),
    val kotlinConstants: List<FileSpec> = listOf(),
    val kotlinClientTypes: List<FileSpec> = listOf(),
    val docFiles: List<DocFileSpec> = listOf()
) {
    companion object {
        val EMPTY = CodeGenResult()
    }
    fun merge(current: CodeGenResult): CodeGenResult {
        if (current === EMPTY) {
            return this
        }
        if (this === EMPTY) {
            return current
        }
        return CodeGenResult(
            javaDataTypes = javaDataTypes.concat(current.javaDataTypes),
            javaInterfaces = javaInterfaces.concat(current.javaInterfaces),
            javaEnumTypes = javaEnumTypes.concat(current.javaEnumTypes),
            javaDataFetchers = javaDataFetchers.concat(current.javaDataFetchers),
            javaQueryTypes = javaQueryTypes.concat(current.javaQueryTypes),
            clientProjections = clientProjections.concat(current.clientProjections).distinct(),
            javaConstants = javaConstants.concat(current.javaConstants),
            kotlinDataTypes = kotlinDataTypes.concat(current.kotlinDataTypes),
            kotlinInputTypes = kotlinInputTypes.concat(current.kotlinInputTypes),
            kotlinInterfaces = kotlinInterfaces.concat(current.kotlinInterfaces),
            kotlinEnumTypes = kotlinEnumTypes.concat(current.kotlinEnumTypes),
            kotlinDataFetchers = kotlinDataFetchers.concat(current.kotlinDataFetchers),
            kotlinConstants = kotlinConstants.concat(current.kotlinConstants),
            kotlinClientTypes = kotlinClientTypes.concat(current.kotlinClientTypes),
            docFiles = docFiles.concat(current.docFiles)
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
            .plus(kotlinInputTypes)
            .plus(kotlinInterfaces)
            .plus(kotlinEnumTypes)
            .plus(kotlinConstants)
            .plus(kotlinClientTypes)
            .toList()
    }

    private fun <T> List<T>.concat(other: List<T>): List<T> {
        if (other.isEmpty()) {
            return this
        }
        if (isEmpty()) {
            return other
        }
        return this + other
    }
}

fun List<FieldDefinition>.filterSkipped(): List<FieldDefinition> {
    return this.filter { it.directives.none { d -> d.name == "skipcodegen" } }
}

fun Sequence<FieldDefinition>.filterSkipped(): Sequence<FieldDefinition> {
    return this.filter { it.directives.none { d -> d.name == "skipcodegen" } }
}

fun List<FieldDefinition>.filterIncludedInConfig(definitionName: String, config: CodeGenConfig): List<FieldDefinition> {
    return when (definitionName) {
        "Query" -> {
            if (config.includeQueries.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeQueries }
            }
        }
        "Mutation" -> {
            if (config.includeMutations.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeMutations }
            }
        }
        "Subscription" -> {
            if (config.includeSubscriptions.isEmpty()) {
                this
            } else {
                this.filter { it.name in config.includeSubscriptions }
            }
        }
        else -> this
    }
}

fun <T : DirectivesContainer<*>> DirectivesContainer<T>.shouldSkip(
    config: CodeGenConfig
): Boolean {
    return directives.any { it.name == "skipcodegen" } || config.typeMapping.containsKey((this as NamedNode<*>).name)
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
    val unwrapped = TypeUtil.unwrapAll(this)
    return if (includeBaseTypes && unwrapped.isBaseType()) {
        unwrapped.findBaseTypeDefinition()
    } else {
        document.definitions.asSequence().filterIsInstance<TypeDefinition<*>>().find {
            if (it is ScalarTypeDefinition) {
                includeScalarTypes && it.name == unwrapped.name
            } else {
                it.name == unwrapped.name && (!excludeExtensions || it !is ObjectTypeExtensionDefinition)
            }
        }
    }
}

private fun TypeName.isBaseType(): Boolean {
    return ScalarInfo.isGraphqlSpecifiedScalar(name)
}

private fun TypeName.findBaseTypeDefinition(): TypeDefinition<*>? {
    return ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS[name]
}

class CodeGenSchemaParsingException(
    schemaReader: Reader,
    invalidSyntaxException: InvalidSyntaxException
) : RuntimeException(buildMessage(schemaReader, invalidSyntaxException), invalidSyntaxException) {
    companion object {
        private fun buildMessage(
            schemaReader: Reader,
            invalidSyntaxException: InvalidSyntaxException
        ): String {
            schemaReader.use { reader ->
                return """
                |Unable to parse the schema...
                |${invalidSyntaxException.message}
                | 
                |Schema Section:
                |>>>
                |${invalidSyntaxException.sourcePreview}
                |<<<
                |
                |Full Schema:
                |${reader.readText()}
                """.trimMargin()
            }
        }
    }
}
