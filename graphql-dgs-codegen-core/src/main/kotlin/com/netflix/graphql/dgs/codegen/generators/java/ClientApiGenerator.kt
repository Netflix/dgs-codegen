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

package com.netflix.graphql.dgs.codegen.generators.java

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery
import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.SchemaIndex
import com.netflix.graphql.dgs.codegen.filterIncludedInConfig
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.findTypeDefinition
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.collectAllFieldDefinitions
import com.palantir.javapoet.ClassName
import com.palantir.javapoet.CodeBlock
import com.palantir.javapoet.FieldSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.ParameterSpec
import com.palantir.javapoet.ParameterizedTypeName
import com.palantir.javapoet.TypeSpec
import com.palantir.javapoet.TypeVariableName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.Directive
import graphql.language.DirectivesContainer
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeDefinition
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.language.VariableDefinition
import java.lang.Deprecated
import javax.lang.model.element.Modifier
import kotlin.Pair
import kotlin.String
import kotlin.let
import kotlin.to

class ClientApiGenerator internal constructor(
    private val config: CodeGenConfig,
    private val schemaIndex: SchemaIndex,
) {
    constructor(config: CodeGenConfig, document: Document) : this(config, SchemaIndex(document))

    private val generatedClasses = mutableSetOf<String>()
    private val typeUtils = TypeUtils(getDatatypesPackageName(), config, schemaIndex)
    private val javaReservedKeywordSanitizer = JavaReservedKeywordSanitizer()

    fun generate(
        definition: ObjectTypeDefinition,
        methodNames: MutableSet<String>,
    ): CodeGenResult = generate(definition, methodNames, rootProjectionTypes(listOf(definition)))

    /**
     * Pass [rootProjectionTypes] computed over every operation, or same-named fields returning different types collide.
     */
    internal fun generate(
        definition: ObjectTypeDefinition,
        methodNames: MutableSet<String>,
        rootProjectionTypes: Map<String, String>,
    ): CodeGenResult =
        rootFields(definition)
            .map {
                val javaFile = createQueryClass(it, definition.name, methodNames)

                val rootProjection =
                    it.type.findTypeDefinition(schemaIndex, true)?.let { typeDefinition ->
                        val prefix = rootProjectionPrefix(it.name.capitalized(), definition.name, typeDefinition, rootProjectionTypes)
                        createRootProjection(typeDefinition, prefix)
                    }
                        ?: CodeGenResult.EMPTY
                CodeGenResult(javaQueryTypes = listOf(javaFile)).merge(rootProjection)
            }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

    fun generateEntities(definitions: List<ObjectTypeDefinition>): CodeGenResult {
        if (config.skipEntityQueries) {
            return CodeGenResult.EMPTY
        }

        // generate for federation types, if present
        val federatedTypes = definitions.filter { it.hasDirective("key") }
        if (federatedTypes.isNotEmpty()) {
            // create entities root projection
            return createEntitiesRootProjection(federatedTypes)
        }
        return CodeGenResult.EMPTY
    }

    private fun createQueryClass(
        it: FieldDefinition,
        operation: String,
        methodNames: MutableSet<String>,
    ): JavaFile {
        val setType = ClassName.get(Set::class.java)
        val stringType = ClassName.get(String::class.java)
        val setOfStringType = ParameterizedTypeName.get(setType, stringType)
        val listOfVariablesType =
            ParameterizedTypeName.get(
                ClassName.get(List::class.java),
                ClassName.get(VariableDefinition::class.java),
            )
        val mapOfStringsType = ParameterizedTypeName.get(ClassName.get(Map::class.java), stringType, stringType)

        val methodName = generateMethodName(it.name.capitalized(), operation.lowercase(), methodNames)
        val javaType =
            TypeSpec
                .classBuilder(methodName)
                .addOptionalGeneratedAnnotation(config)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(GraphQLQuery::class.java))

        if (it.description != null) {
            javaType.addJavadoc("\$L", it.description.content)
        }

        val deprecatedClassDirective = getDeprecateDirective(it)
        if (deprecatedClassDirective != null) {
            javaType.addAnnotation(Deprecated::class.java)
            val deprecationReason = getDeprecatedReason(deprecatedClassDirective)
            if (deprecationReason != null) {
                javaType.addJavadoc("@deprecated \$L", deprecationReason)
            }
        }

        javaType.addMethod(
            MethodSpec
                .methodBuilder("getOperationName")
                .addModifiers(Modifier.PUBLIC)
                .returns(String::class.java)
                .addAnnotation(Override::class.java)
                .addStatement("return \$S", it.name)
                .build(),
        )

        val builderClass =
            TypeSpec
                .classBuilder("Builder")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addOptionalGeneratedAnnotation(config)
                .addMethod(
                    MethodSpec
                        .methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("", methodName))
                        .addCode(
                            if (it.inputValueDefinitions.isNotEmpty()) {
                                """
                            |return new $methodName(${it.inputValueDefinitions.joinToString(
                                    ", ",
                                ) {
                                    javaReservedKeywordSanitizer.sanitize(
                                        it.name,
                                    )
                                }}, queryName, fieldsSet, variableReferences, variableDefinitions);
                            |         
                                """.trimMargin()
                            } else {
                                """
                            |return new $methodName(queryName);                                     
                                """.trimMargin()
                            },
                        ).build(),
                ).addField(
                    FieldSpec
                        .builder(
                            setOfStringType,
                            "fieldsSet",
                            Modifier.PRIVATE,
                        ).initializer("new \$T<>()", ClassName.get(HashSet::class.java))
                        .build(),
                ).addField(
                    FieldSpec
                        .builder(
                            mapOfStringsType,
                            "variableReferences",
                            Modifier.PRIVATE,
                            Modifier.FINAL,
                        ).initializer("new \$T<>()", ClassName.get(HashMap::class.java))
                        .build(),
                ).addField(
                    FieldSpec
                        .builder(
                            listOfVariablesType,
                            "variableDefinitions",
                            Modifier.PRIVATE,
                            Modifier.FINAL,
                        ).initializer("new \$T<>()", ClassName.get(ArrayList::class.java))
                        .build(),
                )

        val constructorBuilder =
            MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
        constructorBuilder.addStatement("super(\$S, queryName)", operation.lowercase())

        val legacyConstructorBuilder =
            MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
        legacyConstructorBuilder.addStatement("super(\$S, queryName)", operation.lowercase())

        it.inputValueDefinitions.forEach { inputValue ->
            val findReturnType = TypeUtils(getDatatypesPackageName(), config, schemaIndex).findReturnType(inputValue.type)
            val sanitizedInputName = javaReservedKeywordSanitizer.sanitize(inputValue.name)

            val deprecatedDirective = getDeprecateDirective(inputValue)
            val deprecationReason = deprecatedDirective?.let { it1 -> getDeprecatedReason(it1) }

            val methodBuilder =
                MethodSpec
                    .methodBuilder(sanitizedInputName)
                    .addParameter(findReturnType, sanitizedInputName)
                    .returns(ClassName.get("", "Builder"))
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        """
                    |this.$sanitizedInputName = $sanitizedInputName;
                    |this.fieldsSet.add("${inputValue.name}");
                    |return this;
                        """.trimMargin(),
                    )

            addDeprecationWarnings(deprecatedDirective, methodBuilder, inputValue, deprecationReason)
            builderClass
                .addMethod(methodBuilder.build())
                .addField(findReturnType, sanitizedInputName, Modifier.PRIVATE)

            val inputValueType = inputValue.type
            val typeForVariableDefinition = getVariableDefinitionType(inputValueType)
            val referenceMethodBuilder =
                MethodSpec
                    .methodBuilder(sanitizedInputName + "Reference")
                    .addParameter(stringType, "variableRef")
                    .returns(ClassName.get("", "Builder"))
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        """
                    |this.variableReferences.put("${inputValue.name}", variableRef);
                    |this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, $typeForVariableDefinition).build());
                    |this.fieldsSet.add("${inputValue.name}");
                    |return this;
                        """.trimMargin(),
                    )

            addDeprecationWarnings(deprecatedDirective, referenceMethodBuilder, inputValue, deprecationReason)
            builderClass.addMethod(referenceMethodBuilder.build())

            constructorBuilder.addParameter(findReturnType, sanitizedInputName)
            legacyConstructorBuilder.addParameter(findReturnType, sanitizedInputName)

            if (findReturnType.isPrimitive) {
                val code =
                    """
                    |getInput().put("${inputValue.name}", $sanitizedInputName);
                    """.trimMargin()
                constructorBuilder.addCode(code)
                legacyConstructorBuilder.addCode(code)
            } else {
                val code =
                    """
                    |if ($sanitizedInputName != null || fieldsSet.contains("${inputValue.name}")) {
                    |    getInput().put("${inputValue.name}", $sanitizedInputName);
                    |}
                    """.trimMargin()
                constructorBuilder.addCode(code)
                legacyConstructorBuilder.addCode(code)
            }
        }

        if (it.inputValueDefinitions.isNotEmpty()) {
            constructorBuilder.addCode(
                """
                |
                |if(variableDefinitions != null) {
                |   getVariableDefinitions().addAll(variableDefinitions);
                |}
                |
                |if(variableReferences != null) {
                |   getVariableReferences().putAll(variableReferences);
                |}                      
                """.trimMargin(),
            )
        }

        val nameMethodBuilder =
            MethodSpec
                .methodBuilder("queryName")
                .addParameter(String::class.java, "queryName")
                .returns(ClassName.get("", "Builder"))
                .addModifiers(Modifier.PUBLIC)
                .addCode(
                    """
                |this.queryName = queryName;
                |return this;
                    """.trimMargin(),
                )

        builderClass
            .addField(FieldSpec.builder(String::class.java, "queryName", Modifier.PRIVATE).build())
            .addMethod(nameMethodBuilder.build())

        constructorBuilder.addParameter(String::class.java, "queryName")
        legacyConstructorBuilder.addParameter(String::class.java, "queryName")

        if (it.inputValueDefinitions.isNotEmpty()) {
            constructorBuilder.addParameter(setOfStringType, "fieldsSet")
            legacyConstructorBuilder.addParameter(setOfStringType, "fieldsSet")
            constructorBuilder.addParameter(mapOfStringsType, "variableReferences")
            constructorBuilder.addParameter(listOfVariablesType, "variableDefinitions")

            // We only need this backward compatible constructor if we added the new fields to the main constructor.
            javaType.addMethod(legacyConstructorBuilder.build())
        }

        javaType.addMethod(constructorBuilder.build())

        // No-arg constructor
        javaType.addMethod(
            MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(\"${operation.lowercase()}\")")
                .build(),
        )

        javaType.addMethod(
            MethodSpec
                .methodBuilder("newRequest")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(ClassName.get("", "Builder"))
                .addCode("return new Builder();\n")
                .build(),
        )
        javaType.addType(builderClass.build())
        return JavaFile.builder(getPackageName(), javaType.build()).build()
    }

    private fun getVariableDefinitionType(inputValueType: Type<*>): String =
        when (inputValueType) {
            is TypeName -> "new graphql.language.TypeName(\"${inputValueType.name}\")"
            is ListType -> "new graphql.language.ListType(${getVariableDefinitionType(inputValueType.type)})"
            is NonNullType -> "new graphql.language.NonNullType(${getVariableDefinitionType(inputValueType.type)})"

            else -> {
                "new graphql.language.TypeName(\"String\")"
            }
        }

    private fun addDeprecationWarnings(
        deprecatedDirective: Directive?,
        methodBuilder: MethodSpec.Builder,
        inputValue: InputValueDefinition,
        deprecationReason: String?,
    ) {
        if (deprecatedDirective != null) {
            methodBuilder.addAnnotation(Deprecated::class.java)
        }

        // Build Javadoc, separate multiple blocks by empty line
        val javaDoc = CodeBlock.builder()

        if (inputValue.description != null) {
            javaDoc.add("\$L", inputValue.description.content)
        }
        if (deprecationReason != null) {
            if (!javaDoc.isEmpty) {
                javaDoc.add("\n\n")
            }
            javaDoc.add("@deprecated \$L", deprecationReason)
        }

        if (!javaDoc.isEmpty) {
            methodBuilder.addJavadoc(javaDoc.build())
        }
    }

    /**
     * Generate method name. If there are same method names in type `Query`, `Mutation` and `Subscription`, add suffix.
     * For example, there are `shows` in `Query`, `Mutation` and `Subscription`, the generated files should be:
     * `ShowsGraphQLQuery`, `ShowsGraphQLMutation` and `ShowsGraphQLSubscription`
     */
    private fun generateMethodName(
        originalMethodName: String,
        typeName: String,
        methodNames: MutableSet<String>,
    ): String =
        if ("mutation" == typeName && methodNames.contains(originalMethodName)) {
            originalMethodName.plus("GraphQLMutation")
        } else if ("subscription" == typeName && methodNames.contains(originalMethodName)) {
            originalMethodName.plus("GraphQLSubscription")
        } else {
            methodNames.add(originalMethodName)
            originalMethodName.plus("GraphQLQuery")
        }

    private fun createProjectionClass(clazzName: String): TypeSpec.Builder {
        val baseProjectionClass = ClassName.get(BaseSubProjectionNode::class.java)
        val baseProjectionType =
            ParameterizedTypeName.get(baseProjectionClass, TypeVariableName.get("?"), TypeVariableName.get("?"))
        val parentType = TypeVariableName.get("PARENT").withBounds(baseProjectionType)
        val rootType = TypeVariableName.get("ROOT").withBounds(baseProjectionType)

        return TypeSpec
            .classBuilder(clazzName)
            .addOptionalGeneratedAnnotation(config)
            .addTypeVariable(parentType)
            .addTypeVariable(rootType)
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(baseProjectionClass, TypeVariableName.get("PARENT"), TypeVariableName.get("ROOT")))
    }

    private fun createRootProjectionConstructor(typeName: String): MethodSpec =
        MethodSpec
            .constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addCode("""super(null, null, java.util.Optional.of("$typeName"));""")
            .build()

    /**
     * Maps each unqualified root projection class name to the type it projects, given [operations] in generation order.
     *
     * Root projections are named after the operation field, so `Query.result: QueryResult` and
     * `Mutation.result: MutationResult` both want `ResultProjectionRoot`.
     * Up to 8.7.0 each operation generated its own, identical ones were deduplicated keeping the first and the last
     * one written replaced the rest on disk.
     * The type that won there keeps the unqualified name, so clients compiled against it still compile:
     * `distinct()` reproduces the deduplication and `toMap()` the last write.
     * [generateEntities] writes `EntitiesProjectionRoot` after every operation, so a Query field named `entities` loses
     * it to [federatedTypes].
     */
    internal fun rootProjectionTypes(
        operations: List<ObjectTypeDefinition>,
        federatedTypes: List<ObjectTypeDefinition> = emptyList(),
    ): Map<String, String> {
        val rootProjectionTypes =
            operations
                .flatMap { operation ->
                    rootFields(operation).mapNotNull { field ->
                        field.type.findTypeDefinition(schemaIndex, true)?.let { "${field.name.capitalized()}ProjectionRoot" to it.name }
                    }
                }.distinct()
                .toMap()
        return if (!config.skipEntityQueries && federatedTypes.isNotEmpty()) {
            rootProjectionTypes + (ENTITIES_PROJECTION_ROOT to "_entities")
        } else {
            rootProjectionTypes
        }
    }

    private fun rootFields(definition: ObjectTypeDefinition): List<FieldDefinition> =
        definition.fieldDefinitions
            .filterIncludedInConfig(definition.name, config)
            .filterSkipped()

    /**
     * A field returning a different type than the unqualified root gets an operation-qualified one such as
     * `ResultGraphQLQueryProjectionRoot`, mirroring the query class name from [generateMethodName].
     */
    private fun rootProjectionPrefix(
        fieldName: String,
        operation: String,
        type: TypeDefinition<*>,
        rootProjectionTypes: Map<String, String>,
    ): String =
        if (rootProjectionTypes["${fieldName}ProjectionRoot"] == type.name) {
            fieldName
        } else {
            "${fieldName}GraphQL${operation.capitalized()}"
        }

    private fun createRootProjection(
        type: TypeDefinition<*>,
        prefix: String,
    ): CodeGenResult {
        val clazzName = "${prefix}ProjectionRoot"
        val javaType =
            createProjectionClass(clazzName)
                .addMethod(createRootProjectionConstructor(type.name))

        val typeVariable = TypeVariableName.get("$clazzName<PARENT, ROOT>")
        javaType.addMethod(
            MethodSpec
                .methodBuilder(TypeNameMetaFieldDef.name)
                .returns(typeVariable)
                .addCode(
                    """
                        |getFields().put("${TypeNameMetaFieldDef.name}", null);
                        |return this;
                    """.trimMargin(),
                ).addModifiers(Modifier.PUBLIC)
                .build(),
        )

        if (generatedClasses.contains(clazzName)) return CodeGenResult.EMPTY else generatedClasses.add(clazzName)

        val fieldDefinitions = collectAllFieldDefinitions(type, schemaIndex)

        val codeGenResult =
            fieldDefinitions
                .filterSkipped()
                .mapNotNull {
                    val typeDefinition =
                        it.type.findTypeDefinition(
                            schemaIndex,
                            excludeExtensions = true,
                            includeBaseTypes = it.inputValueDefinitions.isNotEmpty(),
                            includeScalarTypes = it.inputValueDefinitions.isNotEmpty(),
                        )
                    if (typeDefinition != null) it to typeDefinition else null
                }.map { (fieldDef, typeDef) ->
                    val projectionName = "${typeDef.name.capitalized()}Projection"
                    if (typeDef !is ScalarTypeDefinition) {
                        val projectionTypeVariable =
                            TypeVariableName.get(
                                "$projectionName<$clazzName<PARENT, ROOT>, $clazzName<PARENT, ROOT>>",
                            )
                        val noArgMethodBuilder =
                            MethodSpec
                                .methodBuilder(javaReservedKeywordSanitizer.sanitize(fieldDef.name))
                                .returns(projectionTypeVariable)
                                .addCode(
                                    """
                            |$projectionName<$clazzName<PARENT, ROOT>, $clazzName<PARENT, ROOT>> projection = new $projectionName<>(this, this);    
                            |getFields().put("${fieldDef.name}", projection);
                            |return projection;
                                    """.trimMargin(),
                                ).addModifiers(Modifier.PUBLIC)
                        javaType.addMethod(noArgMethodBuilder.build())
                    }

                    if (fieldDef.inputValueDefinitions.isNotEmpty()) {
                        addFieldSelectionMethodWithArguments(fieldDef, projectionName, javaType, projectionRoot = "this")
                        addFieldSelectionMethodWithArgumentsReferences(fieldDef, projectionName, javaType, projectionRoot = "this")
                    }

                    createSubProjection(
                        typeDef,
                        javaType.build(),
                        typeDef.name.capitalized(),
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {
            val objectTypeDefinition = it.type.findTypeDefinition(schemaIndex)
            if (objectTypeDefinition == null) {
                javaType.addMethod(
                    MethodSpec
                        .methodBuilder(javaReservedKeywordSanitizer.sanitize(it.name))
                        .returns(TypeVariableName.get("$clazzName<PARENT, ROOT>"))
                        .addCode(
                            """
                            |getFields().put("${it.name}", null);
                            |return this;
                            """.trimMargin(),
                        ).addModifiers(Modifier.PUBLIC)
                        .build(),
                )
            }
        }

        val concreteTypesResult = createConcreteTypes(type, javaType.build(), javaType)
        val unionTypesResult = createUnionTypes(type, javaType, javaType.build())

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(
            clientProjections = listOf(javaFile),
        ).merge(codeGenResult).merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun addFieldSelectionMethodWithArguments(
        fieldDefinition: FieldDefinition,
        projectionName: String,
        javaType: TypeSpec.Builder,
        projectionRoot: String,
    ): TypeSpec.Builder? {
        val clazzName = javaType.build().name()
        val rootTypeName = if (projectionRoot == "this") "$clazzName<PARENT, ROOT>" else "ROOT"
        val returnTypeName = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName>")
        val methodBuilder =
            MethodSpec
                .methodBuilder(javaReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                .returns(returnTypeName)
                .addCode(
                    """
                |$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName> projection = new $projectionName<>(this, $projectionRoot);    
                |getFields().put("${fieldDefinition.name}", projection);
                |getInputArguments().computeIfAbsent("${fieldDefinition.name}", k -> new ${'$'}T<>());                      
                |${
                        fieldDefinition.inputValueDefinitions.joinToString("\n") { input ->
                            val sanitizedName = javaReservedKeywordSanitizer.sanitize(input.name)
                            """
                     |InputArgument ${sanitizedName}Arg = new InputArgument("${input.name}", $sanitizedName, false, null);
                     |getInputArguments().get("${fieldDefinition.name}").add(${sanitizedName}Arg);
                            """.trimMargin()
                        }
                    }
                |return projection;
                    """.trimMargin(),
                    ArrayList::class.java,
                ).addModifiers(Modifier.PUBLIC)

        fieldDefinition.inputValueDefinitions.forEach { input ->
            methodBuilder.addParameter(
                ParameterSpec.builder(typeUtils.findReturnType(input.type), javaReservedKeywordSanitizer.sanitize(input.name)).build(),
            )
        }
        return javaType.addMethod(methodBuilder.build())
    }

    private fun addFieldSelectionMethodWithArgumentsReferences(
        fieldDefinition: FieldDefinition,
        projectionName: String,
        javaType: TypeSpec.Builder,
        projectionRoot: String,
    ): TypeSpec.Builder? {
        val clazzName = javaType.build().name()
        val rootTypeName = if (projectionRoot == "this") "$clazzName<PARENT, ROOT>" else "ROOT"
        val returnTypeName = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName>")
        val methodBuilder =
            MethodSpec
                .methodBuilder(javaReservedKeywordSanitizer.sanitize(fieldDefinition.name + "WithVariableReferences"))
                .returns(returnTypeName)
                .addCode(
                    """
                |$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName> projection = new $projectionName<>(this, $projectionRoot);    
                |getFields().put("${fieldDefinition.name}", projection);
                |getInputArguments().computeIfAbsent("${fieldDefinition.name}", k -> new ${'$'}T<>());              
                |${
                        fieldDefinition.inputValueDefinitions.joinToString("\n") { input ->
                            val sanitizedName = javaReservedKeywordSanitizer.sanitize(input.name)
                            """
                     |InputArgument ${sanitizedName}Arg = new InputArgument("${input.name}", ${sanitizedName}Reference, true, ${getVariableDefinitionType(
                                input.type,
                            )});
                     |getInputArguments().get("${fieldDefinition.name}").add(${sanitizedName}Arg);
                            """.trimMargin()
                        }
                    }
                |return projection;
                    """.trimMargin(),
                    ArrayList::class.java,
                ).addModifiers(Modifier.PUBLIC)

        fieldDefinition.inputValueDefinitions.forEach { input ->
            val sanitizedName = javaReservedKeywordSanitizer.sanitize(input.name)
            methodBuilder.addParameter(ParameterSpec.builder(ClassName.get(String::class.java), "${sanitizedName}Reference").build())
        }
        return javaType.addMethod(methodBuilder.build())
    }

    private fun createEntitiesRootProjection(federatedTypes: List<ObjectTypeDefinition>): CodeGenResult {
        val clazzName = ENTITIES_PROJECTION_ROOT
        val javaType =
            createProjectionClass(clazzName)
                .addMethod(createRootProjectionConstructor("_entities"))

        if (generatedClasses.contains(clazzName)) return CodeGenResult.EMPTY else generatedClasses.add(clazzName)

        val codeGenResult =
            federatedTypes
                .map { objTypeDef ->
                    val projectionName = "Entities${objTypeDef.name.capitalized()}KeyProjection"
                    val returnType = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $clazzName<PARENT, ROOT>>")
                    javaType.addMethod(
                        MethodSpec
                            .methodBuilder("on${objTypeDef.name}")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(returnType)
                            .addCode(
                                """
                        | Entities${objTypeDef.name.capitalized()}KeyProjection<$clazzName<PARENT, ROOT>, $clazzName<PARENT, ROOT>> fragment = new Entities${objTypeDef.name.capitalized()}KeyProjection(this, this);
                        | getFragments().add(fragment);
                        | return fragment;
                                """.trimMargin(),
                            ).build(),
                    )
                    createFragment(
                        objTypeDef,
                        javaType.build(),
                        "Entities${objTypeDef.name.capitalized()}Key",
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createConcreteTypes(
        type: TypeDefinition<*>,
        root: TypeSpec,
        javaType: TypeSpec.Builder,
    ): CodeGenResult =
        if (type is InterfaceTypeDefinition) {
            val concreteTypes = schemaIndex.implementations(type.name).distinctBy { it.name }
            concreteTypes
                .map {
                    addFragmentProjectionMethod(javaType, root, it)
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
        } else {
            CodeGenResult.EMPTY
        }

    private fun createUnionTypes(
        type: TypeDefinition<*>,
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
    ): CodeGenResult =
        if (type is UnionTypeDefinition) {
            val memberTypes = type.memberTypes.mapNotNull { it.findTypeDefinition(schemaIndex, true) }.toList()
            memberTypes
                .map {
                    addFragmentProjectionMethod(javaType, rootType, it)
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
        } else {
            CodeGenResult.EMPTY
        }

    private fun addFragmentProjectionMethod(
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
        it: TypeDefinition<*>,
    ): CodeGenResult {
        val rootRef = if (javaType.build().name() == rootType.name()) "this" else "getRoot()"
        val rootTypeName = if (javaType.build().name() == rootType.name()) "${rootType.name()}<PARENT, ROOT>" else "ROOT"
        val parentRef = javaType.build().name()
        val projectionName = "${it.name.capitalized()}Fragment"
        val fullProjectionName = "${projectionName}Projection"
        val typeVariable = TypeVariableName.get("$fullProjectionName<$parentRef<PARENT, ROOT>, $rootTypeName>")
        javaType.addMethod(
            MethodSpec
                .methodBuilder("on${it.name}")
                .addModifiers(Modifier.PUBLIC)
                .returns(typeVariable)
                .addCode(
                    """
                    |$fullProjectionName<$parentRef<PARENT, ROOT>, $rootTypeName> fragment = new $fullProjectionName<>(this, $rootRef);
                    |getFragments().add(fragment);
                    |return fragment;
                    """.trimMargin(),
                ).build(),
        )

        return createFragment(it as ObjectTypeDefinition, rootType, projectionName)
    }

    private fun createFragment(
        type: ObjectTypeDefinition,
        root: TypeSpec,
        prefix: String,
    ): CodeGenResult {
        val subProjection =
            createSubProjectionType(type, root, prefix)
                ?: return CodeGenResult.EMPTY
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        // We don't need the typename added for fragments in the entities' projection.
        // This affects deserialization when use directly with generated classes
        if (prefix != "Entities${type.name.capitalized()}Key") {
            javaType.addInitializerBlock(
                CodeBlock
                    .builder()
                    .addStatement("getFields().put(\$S, null)", TypeNameMetaFieldDef.name)
                    .build(),
            )
        }

        javaType.addMethod(
            MethodSpec
                .methodBuilder("toString")
                .returns(ClassName.get(String::class.java))
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addCode(
                    """
                    |StringBuilder builder = new StringBuilder();
                    |builder.append("... on ${type.name} {");
                    |getFields().forEach((k, v) -> {
                    |    builder.append(" ").append(k);
                    |    if(v != null) {
                    |        builder.append(" ").append(v.toString());
                    |    }
                    |});
                    |builder.append("}");
                    | 
                    |return builder.toString();
                    """.trimMargin(),
                ).build(),
        )

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjection(
        type: TypeDefinition<*>,
        root: TypeSpec,
        prefix: String,
    ): CodeGenResult {
        val subProjection =
            createSubProjectionType(type, root, prefix)
                ?: return CodeGenResult.EMPTY
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjectionType(
        type: TypeDefinition<*>,
        root: TypeSpec,
        prefix: String,
    ): Pair<TypeSpec.Builder, CodeGenResult>? {
        val clazzName = "${prefix}Projection"
        if (generatedClasses.contains(clazzName)) return null else generatedClasses.add(clazzName)

        val javaType =
            createProjectionClass(clazzName)
                .addMethod(
                    MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(ClassName.get("", "PARENT"), "parent").build())
                        .addParameter(ParameterSpec.builder(ClassName.get("", "ROOT"), "root").build())
                        .addCode("""super(parent, root, java.util.Optional.of("${type.name}"));""")
                        .build(),
                )

        // add a method for setting the __typename
        val typeVariable = TypeVariableName.get("$clazzName<PARENT, ROOT>")
        javaType.addMethod(
            MethodSpec
                .methodBuilder(TypeNameMetaFieldDef.name)
                .returns(typeVariable)
                .addCode(
                    """
                        |getFields().put("${TypeNameMetaFieldDef.name}", null);
                        |return this;
                    """.trimMargin(),
                ).addModifiers(Modifier.PUBLIC)
                .build(),
        )

        val fieldDefinitions = collectAllFieldDefinitions(type, schemaIndex)

        val codeGenResult =
            fieldDefinitions
                .filterSkipped()
                .mapNotNull {
                    val typeDefinition = it.type.findTypeDefinition(schemaIndex, true)
                    if (typeDefinition != null) it to typeDefinition else null
                }.map { (fieldDef, typeDef) ->
                    val projectionName = "${typeDef.name.capitalized()}Projection"
                    val methodName = javaReservedKeywordSanitizer.sanitize(fieldDef.name)
                    val projectionTypeVariable = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, ROOT>")
                    javaType.addMethod(
                        MethodSpec
                            .methodBuilder(methodName)
                            .returns(projectionTypeVariable)
                            .addCode(
                                """
                                    | $projectionName<$clazzName<PARENT, ROOT>, ROOT> projection = new $projectionName<>(this, getRoot());
                                    | getFields().put("${fieldDef.name}", projection);
                                    | return projection;
                                """.trimMargin(),
                            ).addModifiers(Modifier.PUBLIC)
                            .build(),
                    )

                    if (fieldDef.inputValueDefinitions.isNotEmpty()) {
                        addFieldSelectionMethodWithArguments(fieldDef, projectionName, javaType, projectionRoot = "getRoot()")
                        addFieldSelectionMethodWithArgumentsReferences(fieldDef, projectionName, javaType, projectionRoot = "getRoot()")
                    }

                    createSubProjection(
                        typeDef,
                        root,
                        typeDef.name.capitalized(),
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions
            .filterSkipped()
            .forEach {
                val objectTypeDefinition = it.type.findTypeDefinition(schemaIndex)
                if (objectTypeDefinition == null) {
                    javaType.addMethod(
                        MethodSpec
                            .methodBuilder(javaReservedKeywordSanitizer.sanitize(it.name))
                            .returns(TypeVariableName.get("$clazzName<PARENT, ROOT>"))
                            .addCode(
                                """
                                |getFields().put("${it.name}", null);
                                |return this;
                                """.trimMargin(),
                            ).addModifiers(Modifier.PUBLIC)
                            .build(),
                    )

                    if (it.inputValueDefinitions.isNotEmpty()) {
                        val methodWithInputArgumentsBuilder =
                            MethodSpec
                                .methodBuilder(javaReservedKeywordSanitizer.sanitize(it.name))
                                .returns(TypeVariableName.get("$clazzName<PARENT, ROOT>"))
                                .addCode(
                                    """
                                |getFields().put("${it.name}", null);
                                |getInputArguments().computeIfAbsent("${it.name}", k -> new ${'$'}T<>());
                                |${
                                        it.inputValueDefinitions.joinToString("\n") { input ->
                                            val sanitizedName = javaReservedKeywordSanitizer.sanitize(input.name)
                                            """
                                     |InputArgument ${sanitizedName}Arg = new InputArgument("${input.name}", $sanitizedName, false, null);
                                     |getInputArguments().get("${it.name}").add(${sanitizedName}Arg);
                                            """.trimMargin()
                                        }}
                                |return this;
                                    """.trimMargin(),
                                    ArrayList::class.java,
                                ).addModifiers(Modifier.PUBLIC)

                        it.inputValueDefinitions.forEach { input ->
                            val sanitizedName = javaReservedKeywordSanitizer.sanitize(input.name)
                            methodWithInputArgumentsBuilder.addParameter(
                                ParameterSpec.builder(typeUtils.findReturnType(input.type), sanitizedName).build(),
                            )
                        }

                        javaType.addMethod(methodWithInputArgumentsBuilder.build())
                    }
                }
            }

        val concreteTypesResult = createConcreteTypes(type, root, javaType)
        val unionTypesResult = createUnionTypes(type, javaType, root)

        return javaType to codeGenResult.merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun getDeprecateDirective(node: DirectivesContainer<*>): Directive? {
        if (config.addDeprecatedAnnotation) {
            return node
                .getDirectives("deprecated")
                ?.firstOrNull() // Should we throw here, if there are multiple "@deprecated"?
        }
        return null
    }

    private fun getDeprecatedReason(directive: Directive): String? =
        directive
            .getArgument("reason")
            ?.let { it.value as? StringValue }
            ?.value

    private fun getPackageName(): String = config.packageNameClient

    private fun getDatatypesPackageName(): String = config.packageNameTypes

    private companion object {
        const val ENTITIES_PROJECTION_ROOT = "EntitiesProjectionRoot"
    }
}
