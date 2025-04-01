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
import com.netflix.graphql.dgs.codegen.fieldDefinitions
import com.netflix.graphql.dgs.codegen.filterIncludedInConfig
import com.netflix.graphql.dgs.codegen.filterSkipped
import com.netflix.graphql.dgs.codegen.findTypeDefinition
import com.netflix.graphql.dgs.codegen.generators.shared.CodeGeneratorUtils.capitalized
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.Directive
import graphql.language.DirectivesContainer
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InputValueDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.ListType
import graphql.language.NamedNode
import graphql.language.NonNullType
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.Type
import graphql.language.TypeDefinition
import graphql.language.TypeName
import graphql.language.UnionTypeDefinition
import graphql.language.VariableDefinition
import java.lang.Deprecated
import javax.lang.model.element.Modifier
import kotlin.Int
import kotlin.Pair
import kotlin.String
import kotlin.let
import kotlin.to

class ClientApiGenerator(
    private val config: CodeGenConfig,
    private val document: Document,
) {
    private val generatedClasses = mutableSetOf<String>()
    private val typeUtils = TypeUtils(getDatatypesPackageName(), config, document)

    fun generate(
        definition: ObjectTypeDefinition,
        methodNames: MutableSet<String>,
    ): CodeGenResult =
        definition.fieldDefinitions
            .filterIncludedInConfig(definition.name, config)
            .filterSkipped()
            .map {
                val javaFile = createQueryClass(it, definition.name, methodNames)

                val rootProjection =
                    it.type.findTypeDefinition(document, true)?.let { typeDefinition ->
                        createRootProjection(typeDefinition, it.name.capitalized())
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
        val listOfVariablesType = ParameterizedTypeName.get(ClassName.get(List::class.java), ClassName.get(VariableDefinition::class.java))
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
            javaType.addAnnotation(java.lang.Deprecated::class.java)
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
                                    ReservedKeywordSanitizer.sanitize(
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

        it.inputValueDefinitions.forEach { inputValue ->
            val findReturnType = TypeUtils(getDatatypesPackageName(), config, document).findReturnType(inputValue.type)

            val deprecatedDirective = getDeprecateDirective(inputValue)
            val deprecationReason = deprecatedDirective?.let { it1 -> getDeprecatedReason(it1) }

            val methodBuilder =
                MethodSpec
                    .methodBuilder(ReservedKeywordSanitizer.sanitize(inputValue.name))
                    .addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))
                    .returns(ClassName.get("", "Builder"))
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        """
                    |this.${ReservedKeywordSanitizer.sanitize(inputValue.name)} = ${ReservedKeywordSanitizer.sanitize(inputValue.name)};
                    |this.fieldsSet.add("${inputValue.name}");
                    |return this;
                        """.trimMargin(),
                    )

            addDeprecationWarnings(deprecatedDirective, methodBuilder, inputValue, deprecationReason)
            builderClass
                .addMethod(methodBuilder.build())
                .addField(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name), Modifier.PRIVATE)

            val inputValueType = inputValue.type
            val typeForVariableDefinition = getVariableDefinitionType(inputValueType)
            val referenceMethodBuilder =
                MethodSpec
                    .methodBuilder(ReservedKeywordSanitizer.sanitize(inputValue.name) + "Reference")
                    .addParameter(stringType, "variableRef")
                    .returns(ClassName.get("", "Builder"))
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(
                        """
                    |this.variableReferences.put("${ReservedKeywordSanitizer.sanitize(inputValue.name)}", variableRef);
                    |this.variableDefinitions.add(graphql.language.VariableDefinition.newVariableDefinition(variableRef, $typeForVariableDefinition).build());
                    |this.fieldsSet.add("${inputValue.name}");
                    |return this;
                        """.trimMargin(),
                    )

            addDeprecationWarnings(deprecatedDirective, referenceMethodBuilder, inputValue, deprecationReason)
            builderClass.addMethod(referenceMethodBuilder.build())

            constructorBuilder.addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))

            if (findReturnType.isPrimitive) {
                constructorBuilder.addCode(
                    """
                    |getInput().put("${inputValue.name}", ${ReservedKeywordSanitizer.sanitize(inputValue.name)});                   
                    """.trimMargin(),
                )
            } else {
                constructorBuilder.addCode(
                    """
                    |if (${ReservedKeywordSanitizer.sanitize(inputValue.name)} != null || fieldsSet.contains("${inputValue.name}")) {
                    |    getInput().put("${inputValue.name}", ${ReservedKeywordSanitizer.sanitize(inputValue.name)});
                    |}
                    """.trimMargin(),
                )
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

        if (it.inputValueDefinitions.size > 0) {
            constructorBuilder.addParameter(setOfStringType, "fieldsSet")
            constructorBuilder.addParameter(mapOfStringsType, "variableReferences")
            constructorBuilder.addParameter(listOfVariablesType, "variableDefinitions")
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

    private fun createRootProjection(
        type: TypeDefinition<*>,
        prefix: String,
    ): CodeGenResult {
        val clazzName = "${prefix}ProjectionRoot"
        val className = ClassName.get(BaseSubProjectionNode::class.java)
        val parentJavaType =
            TypeVariableName
                .get(
                    "PARENT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val rootJavaType =
            TypeVariableName
                .get(
                    "ROOT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val javaType =
            TypeSpec
                .classBuilder(clazzName)
                .addOptionalGeneratedAnnotation(config)
                .addTypeVariable(parentJavaType)
                .addTypeVariable(rootJavaType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(className, TypeVariableName.get("PARENT"), TypeVariableName.get("ROOT")))
                .addMethod(
                    MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("""super(null, null, java.util.Optional.of("${type.name}"));""")
                        .build(),
                )

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

        val fieldDefinitions =
            type.fieldDefinitions() +
                document.definitions
                    .filterIsInstance<ObjectTypeExtensionDefinition>()
                    .filter { it.name == type.name }
                    .flatMap { it.fieldDefinitions }

        val codeGenResult =
            fieldDefinitions
                .filterSkipped()
                .mapNotNull {
                    val typeDefinition =
                        it.type.findTypeDefinition(
                            document,
                            excludeExtensions = true,
                            includeBaseTypes = it.inputValueDefinitions.isNotEmpty(),
                            includeScalarTypes = it.inputValueDefinitions.isNotEmpty(),
                        )
                    if (typeDefinition != null) it to typeDefinition else null
                }.map { (fieldDef, typeDef) ->
                    val projectionName = "${typeDef.name.capitalized()}Projection"
                    if (typeDef !is ScalarTypeDefinition) {
                        val typeVariable = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $clazzName<PARENT, ROOT>>")
                        val noArgMethodBuilder =
                            MethodSpec
                                .methodBuilder(ReservedKeywordSanitizer.sanitize(fieldDef.name))
                                .returns(typeVariable)
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

                    val processedEdges = mutableSetOf<Pair<String, String>>()
                    processedEdges.add(typeDef.name to type.name)
                    createSubProjection(
                        typeDef,
                        javaType.build(),
                        javaType.build(),
                        typeDef.name.capitalized(),
                        processedEdges,
                        1,
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {
            val objectTypeDefinition = it.type.findTypeDefinition(document)
            if (objectTypeDefinition == null) {
                val typeVariable = TypeVariableName.get("$clazzName<PARENT, ROOT>")
                javaType.addMethod(
                    MethodSpec
                        .methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                        .returns(typeVariable)
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

        val concreteTypesResult = createConcreteTypes(type, javaType.build(), javaType, prefix, mutableSetOf(), 0)
        val unionTypesResult = createUnionTypes(type, javaType, javaType.build(), prefix, mutableSetOf(), 0)

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult).merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun addFieldSelectionMethodWithArguments(
        fieldDefinition: FieldDefinition,
        projectionName: String,
        javaType: TypeSpec.Builder,
        projectionRoot: String,
    ): TypeSpec.Builder? {
        val clazzName = javaType.build().name
        val rootTypeName = if (projectionRoot == "this") "$clazzName<PARENT, ROOT>" else "ROOT"
        val returnTypeName = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName>")
        val methodBuilder =
            MethodSpec
                .methodBuilder(ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
                .returns(returnTypeName)
                .addCode(
                    """
                |$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName> projection = new $projectionName<>(this, $projectionRoot);    
                |getFields().put("${fieldDefinition.name}", projection);
                |getInputArguments().computeIfAbsent("${fieldDefinition.name}", k -> new ${'$'}T<>());                      
                |${
                        fieldDefinition.inputValueDefinitions.joinToString("\n") { input ->
                            """
                     |InputArgument ${input.name}Arg = new InputArgument("${input.name}", ${input.name}, false, null);
                     |getInputArguments().get("${fieldDefinition.name}").add(${input.name}Arg);
                            """.trimMargin()
                        }
                    }
                |return projection;
                    """.trimMargin(),
                    ArrayList::class.java,
                ).addModifiers(Modifier.PUBLIC)

        fieldDefinition.inputValueDefinitions.forEach { input ->
            methodBuilder.addParameter(ParameterSpec.builder(typeUtils.findReturnType(input.type), input.name).build())
        }
        return javaType.addMethod(methodBuilder.build())
    }

    private fun addFieldSelectionMethodWithArgumentsReferences(
        fieldDefinition: FieldDefinition,
        projectionName: String,
        javaType: TypeSpec.Builder,
        projectionRoot: String,
    ): TypeSpec.Builder? {
        val clazzName = javaType.build().name
        val rootTypeName = if (projectionRoot == "this") "$clazzName<PARENT, ROOT>" else "ROOT"
        val returnTypeName = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName>")
        val methodBuilder =
            MethodSpec
                .methodBuilder(ReservedKeywordSanitizer.sanitize(fieldDefinition.name + "WithVariableReferences"))
                .returns(returnTypeName)
                .addCode(
                    """
                |$projectionName<$clazzName<PARENT, ROOT>, $rootTypeName> projection = new $projectionName<>(this, $projectionRoot);    
                |getFields().put("${fieldDefinition.name}", projection);
                |getInputArguments().computeIfAbsent("${fieldDefinition.name}", k -> new ${'$'}T<>());              
                |${
                        fieldDefinition.inputValueDefinitions.joinToString("\n") { input ->
                            """
                     |InputArgument ${input.name}Arg = new InputArgument("${input.name}", ${input.name}Reference, true, ${getVariableDefinitionType(
                                input.type,
                            )});
                     |getInputArguments().get("${fieldDefinition.name}").add(${input.name}Arg);
                            """.trimMargin()
                        }
                    }
                |return projection;
                    """.trimMargin(),
                    ArrayList::class.java,
                ).addModifiers(Modifier.PUBLIC)

        fieldDefinition.inputValueDefinitions.forEach { input ->
            methodBuilder.addParameter(ParameterSpec.builder(ClassName.get(String::class.java), "${input.name}Reference").build())
        }
        return javaType.addMethod(methodBuilder.build())
    }

    private fun createEntitiesRootProjection(federatedTypes: List<ObjectTypeDefinition>): CodeGenResult {
        val clazzName = "EntitiesProjectionRoot"
        val className = ClassName.get(BaseSubProjectionNode::class.java)
        val parentType =
            TypeVariableName
                .get(
                    "PARENT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val rootType =
            TypeVariableName
                .get(
                    "ROOT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val javaType =
            TypeSpec
                .classBuilder(clazzName)
                .addOptionalGeneratedAnnotation(config)
                .addTypeVariable(parentType)
                .addTypeVariable(rootType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(className, TypeVariableName.get("PARENT"), TypeVariableName.get("ROOT")))
                .addMethod(
                    MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addCode("""super(null, null, java.util.Optional.of("${"_entities"}"));""")
                        .build(),
                )

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
                    val processedEdges = mutableSetOf<Pair<String, String>>()
                    createFragment(
                        objTypeDef,
                        javaType.build(),
                        javaType.build(),
                        "Entities${objTypeDef.name.capitalized()}Key",
                        processedEdges,
                        0,
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createConcreteTypes(
        type: TypeDefinition<*>,
        root: TypeSpec,
        javaType: TypeSpec.Builder,
        prefix: String,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): CodeGenResult =
        if (type is InterfaceTypeDefinition) {
            val concreteTypes =
                document.getDefinitionsOfType(ObjectTypeDefinition::class.java).filter {
                    it.implements.filterIsInstance<NamedNode<*>>().any { iface -> iface.name == type.name }
                }
            concreteTypes
                .map {
                    addFragmentProjectionMethod(javaType, root, prefix, it, processedEdges, queryDepth)
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
        } else {
            CodeGenResult.EMPTY
        }

    private fun createUnionTypes(
        type: TypeDefinition<*>,
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
        prefix: String,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): CodeGenResult =
        if (type is UnionTypeDefinition) {
            val memberTypes = type.memberTypes.mapNotNull { it.findTypeDefinition(document, true) }.toList()
            memberTypes
                .map {
                    addFragmentProjectionMethod(javaType, rootType, prefix, it, processedEdges, queryDepth)
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
        } else {
            CodeGenResult.EMPTY
        }

    private fun addFragmentProjectionMethod(
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
        prefix: String,
        it: TypeDefinition<*>,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): CodeGenResult {
        val rootRef = if (javaType.build().name == rootType.name) "this" else "getRoot()"
        val rootTypeName = if (javaType.build().name == rootType.name) "${rootType.name}<PARENT, ROOT>" else "ROOT"
        val parentRef = javaType.build().name
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

        return createFragment(it as ObjectTypeDefinition, javaType.build(), rootType, projectionName, processedEdges, queryDepth)
    }

    private fun createFragment(
        type: ObjectTypeDefinition,
        parent: TypeSpec,
        root: TypeSpec,
        prefix: String,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): CodeGenResult {
        val subProjection =
            createSubProjectionType(type, parent, root, prefix, processedEdges, queryDepth)
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
        parent: TypeSpec,
        root: TypeSpec,
        prefix: String,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): CodeGenResult {
        val subProjection =
            createSubProjectionType(type, parent, root, prefix, processedEdges, queryDepth)
                ?: return CodeGenResult.EMPTY
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjectionType(
        type: TypeDefinition<*>,
        parent: TypeSpec,
        root: TypeSpec,
        prefix: String,
        processedEdges: Set<Pair<String, String>>,
        queryDepth: Int,
    ): Pair<TypeSpec.Builder, CodeGenResult>? {
        val className = ClassName.get(BaseSubProjectionNode::class.java)
        val clazzName = "${prefix}Projection"
        if (generatedClasses.contains(clazzName)) return null else generatedClasses.add(clazzName)

        val parentJavaType =
            TypeVariableName
                .get(
                    "PARENT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val rootJavaType =
            TypeVariableName
                .get(
                    "ROOT",
                ).withBounds(ParameterizedTypeName.get(className, TypeVariableName.get("?"), TypeVariableName.get("?")))
        val javaType =
            TypeSpec
                .classBuilder(clazzName)
                .addOptionalGeneratedAnnotation(config)
                .addTypeVariable(parentJavaType)
                .addTypeVariable(rootJavaType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(className, TypeVariableName.get("PARENT"), TypeVariableName.get("ROOT")))
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

        val fieldDefinitions =
            type.fieldDefinitions() +
                document.definitions
                    .filterIsInstance<ObjectTypeExtensionDefinition>()
                    .filter { it.name == type.name }
                    .flatMap { it.fieldDefinitions }

        val codeGenResult =
            fieldDefinitions
                .filterSkipped()
                .mapNotNull {
                    val typeDefinition = it.type.findTypeDefinition(document, true)
                    if (typeDefinition != null) it to typeDefinition else null
                }.map { (fieldDef, typeDef) ->
                    val projectionName = "${typeDef.name.capitalized()}Projection"
                    val methodName = ReservedKeywordSanitizer.sanitize(fieldDef.name)
                    val typeVariable = TypeVariableName.get("$projectionName<$clazzName<PARENT, ROOT>, ROOT>")
                    javaType.addMethod(
                        MethodSpec
                            .methodBuilder(methodName)
                            .returns(typeVariable)
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

                    val updatedProcessedEdges = processedEdges.toMutableSet()
                    updatedProcessedEdges.add(typeDef.name to type.name)
                    createSubProjection(
                        typeDef,
                        javaType.build(),
                        root,
                        typeDef.name.capitalized(),
                        updatedProcessedEdges,
                        queryDepth + 1,
                    )
                }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions
            .filterSkipped()
            .forEach {
                val objectTypeDefinition = it.type.findTypeDefinition(document)
                if (objectTypeDefinition == null) {
                    val typeVariable = TypeVariableName.get("$clazzName<PARENT, ROOT>")
                    javaType.addMethod(
                        MethodSpec
                            .methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                            .returns(typeVariable)
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
                                .methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                                .returns(ClassName.get(getPackageName(), javaType.build().name))
                                .addCode(
                                    """
                                |getFields().put("${it.name}", null);
                                |getInputArguments().computeIfAbsent("${it.name}", k -> new ${'$'}T<>());
                                |${
                                        it.inputValueDefinitions.joinToString("\n") { input ->
                                            """
                                     |InputArgument ${input.name}Arg = new InputArgument("${input.name}", ${input.name}, false, null);
                                     |getInputArguments().get("${it.name}").add(${input.name}Arg);
                                            """.trimMargin()
                                        }}
                                |return this;
                                    """.trimMargin(),
                                    ArrayList::class.java,
                                ).addModifiers(Modifier.PUBLIC)

                        it.inputValueDefinitions.forEach { input ->
                            methodWithInputArgumentsBuilder.addParameter(
                                ParameterSpec.builder(typeUtils.findReturnType(input.type), input.name).build(),
                            )
                        }

                        javaType.addMethod(methodWithInputArgumentsBuilder.build())
                    }
                }
            }

        val concreteTypesResult = createConcreteTypes(type, root, javaType, prefix, processedEdges, queryDepth)
        val unionTypesResult = createUnionTypes(type, javaType, root, prefix, processedEdges, queryDepth)

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
}
