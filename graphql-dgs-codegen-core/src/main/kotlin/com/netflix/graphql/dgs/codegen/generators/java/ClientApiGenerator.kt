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
import com.netflix.graphql.dgs.codegen.*
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
import com.squareup.javapoet.WildcardTypeName
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.Directive
import graphql.language.DirectivesContainer
import graphql.language.Document
import graphql.language.FieldDefinition
import graphql.language.InterfaceTypeDefinition
import graphql.language.NamedNode
import graphql.language.ObjectTypeDefinition
import graphql.language.ObjectTypeExtensionDefinition
import graphql.language.OperationDefinition
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import graphql.language.TypeDefinition
import graphql.language.UnionTypeDefinition
import java.lang.StringBuilder
import java.util.Optional
import javax.lang.model.element.Modifier

class ClientApiGenerator(private val config: CodeGenConfig, private val document: Document) {

    companion object {
        private val PARENT = TypeVariableName.get("PARENT")
        private val ROOT = TypeVariableName.get("ROOT")
        private val ARRAYLIST = ClassName.get(ArrayList::class.java)
        private val HASHSET = ClassName.get(HashSet::class.java)
        private val OPTIONAL = ClassName.get(Optional::class.java)
        private val SET = ClassName.get(Set::class.java)
        private val STRING = ClassName.get(String::class.java)
        private val WILDCARD = WildcardTypeName.subtypeOf(ClassName.OBJECT)
        private val GRAPHQL_QUERY = ClassName.get(GraphQLQuery::class.java)
        private val BASE_SUB_PROJECTION_NODE = ClassName.get(BaseSubProjectionNode::class.java)
    }

    private val generatedClasses = mutableSetOf<String>()
    private val typeUtils = TypeUtils(getDatatypesPackageName(), config, document)

    fun generate(definition: ObjectTypeDefinition): CodeGenResult {
        val operation = OperationDefinition.Operation.valueOf(definition.name.uppercase())
        return definition.fieldDefinitions.asSequence().filterIncludedInConfig(definition.name, config).filterSkipped().map { fieldDef ->
            val javaFile = createQueryClass(fieldDefinition = fieldDef, operation = operation)

            val rootProjection = fieldDef.type.findTypeDefinition(document, true)?.let { typeDefinition ->
                createRootProjection(type = typeDefinition, prefix = fieldDef.name.capitalized())
            } ?: CodeGenResult.EMPTY
            CodeGenResult(javaQueryTypes = listOf(javaFile)).merge(rootProjection)
        }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
    }

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

    private fun createQueryClass(fieldDefinition: FieldDefinition, operation: OperationDefinition.Operation): JavaFile {
        val className = generateClassName(fieldDefinition.name.capitalized(), operation)
        val javaType = TypeSpec.classBuilder(className)
            .addOptionalGeneratedAnnotation(config)
            .addModifiers(Modifier.PUBLIC)
            .superclass(GRAPHQL_QUERY)

        if (fieldDefinition.description != null) {
            javaType.addJavadoc("\$L", fieldDefinition.description.content)
        }

        val deprecatedClassDirective = getDeprecateDirective(fieldDefinition)
        if (deprecatedClassDirective != null) {
            javaType.addAnnotation(java.lang.Deprecated::class.java)
            val deprecationReason = getDeprecatedReason(deprecatedClassDirective)
            if (deprecationReason != null) {
                javaType.addJavadoc("@deprecated \$L", deprecationReason)
            }
        }

        javaType.addMethod(
            MethodSpec.methodBuilder("getOperationName")
                .addModifiers(Modifier.PUBLIC)
                .returns(STRING)
                .addAnnotation(Override::class.java)
                .addStatement("return \$S", fieldDefinition.name)
                .build()
        )

        val setOfStringType = ParameterizedTypeName.get(SET, STRING)

        val buildMethod = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .returns(className)
        if (fieldDefinition.inputValueDefinitions.isNotEmpty()) {
            buildMethod.addStatement(
                "return new \$T(\$L, queryName, fieldsSet)",
                className,
                CodeBlock.join(fieldDefinition.inputValueDefinitions.map { CodeBlock.of("\$L", ReservedKeywordSanitizer.sanitize(it.name)) }, ", ")
            )
        } else {
            buildMethod.addStatement("return new \$T(queryName)", className)
        }

        val builderClass = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
            .addOptionalGeneratedAnnotation(config)
            .addMethod(buildMethod.build())
            .addField(FieldSpec.builder(setOfStringType, "fieldsSet", Modifier.PRIVATE).initializer("new \$T<>()", HASHSET).build())

        val constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
        constructorBuilder.addStatement("super(\$S, queryName)", operation.name.lowercase())

        fieldDefinition.inputValueDefinitions.forEach { inputValue ->
            val findReturnType = TypeUtils(getDatatypesPackageName(), config, document).findReturnType(inputValue.type)

            val deprecatedDirective = getDeprecateDirective(inputValue)
            val deprecationReason = deprecatedDirective?.let { it1 -> getDeprecatedReason(it1) }

            val methodBuilder = MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(inputValue.name))
                .addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))
                .returns(ClassName.get("", "Builder"))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.\$L = \$L", ReservedKeywordSanitizer.sanitize(inputValue.name), ReservedKeywordSanitizer.sanitize(inputValue.name))
                .addStatement("this.fieldsSet.add(\$S)", inputValue.name)
                .addStatement("return this")

            if (deprecatedDirective != null) {
                methodBuilder.addAnnotation(java.lang.Deprecated::class.java)
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

            builderClass.addMethod(methodBuilder.build())
                .addField(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name), Modifier.PRIVATE)

            constructorBuilder.addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))

            if (findReturnType.isPrimitive) {
                constructorBuilder.addStatement("getInput().put(\$S, \$L)", inputValue.name, ReservedKeywordSanitizer.sanitize(inputValue.name))
            } else {
                constructorBuilder.addCode(
                    CodeBlock.builder()
                        .beginControlFlow("if (\$L != null || fieldsSet.contains(\$S))", ReservedKeywordSanitizer.sanitize(inputValue.name), inputValue.name)
                        .addStatement("getInput().put(\$S, \$L)", inputValue.name, ReservedKeywordSanitizer.sanitize(inputValue.name))
                        .endControlFlow()
                        .build()
                )
            }
        }

        val nameMethodBuilder = MethodSpec.methodBuilder("queryName")
            .addParameter(STRING, "queryName")
            .returns(ClassName.get("", "Builder"))
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.queryName = queryName")
            .addStatement("return this")

        builderClass.addField(FieldSpec.builder(STRING, "queryName", Modifier.PRIVATE).build())
            .addMethod(nameMethodBuilder.build())

        constructorBuilder.addParameter(STRING, "queryName")

        if (fieldDefinition.inputValueDefinitions.isNotEmpty()) {
            constructorBuilder.addParameter(setOfStringType, "fieldsSet")
        }

        javaType.addMethod(constructorBuilder.build())

        // No-arg constructor
        javaType.addMethod(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(\$S)", operation.name.lowercase())
                .build()
        )

        javaType.addMethod(
            MethodSpec.methodBuilder("newRequest")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(ClassName.get("", "Builder"))
                .addStatement("return new Builder()")
                .build()
        )
        javaType.addType(builderClass.build())
        return JavaFile.builder(getPackageName(), javaType.build()).build()
    }

    /**
     * Generate method name. If there are same method names in type `Query`, `Mutation` and `Subscription`, add suffix.
     * For example, there are `shows` in `Query`, `Mutation` and `Subscription`, the generated files should be:
     * `ShowsGraphQLQuery`, `ShowsGraphQLMutation` and `ShowsGraphQLSubscription`
     */
    private fun generateClassName(fieldName: String, operation: OperationDefinition.Operation): ClassName {
        return when (operation) {
            OperationDefinition.Operation.QUERY -> ClassName.get(getPackageName(), fieldName + "GraphQLQuery")
            OperationDefinition.Operation.MUTATION -> ClassName.get(getPackageName(), fieldName + "GraphQLMutation")
            OperationDefinition.Operation.SUBSCRIPTION -> ClassName.get(getPackageName(), fieldName + "GraphQLSubscription")
            else -> throw IllegalStateException("Unknown operation: $operation")
        }
    }

    private fun createRootProjection(type: TypeDefinition<*>, prefix: String): CodeGenResult {
        val rootProjectionClass = ClassName.get(getPackageName(), "${prefix}ProjectionRoot")
        val parentJavaType = PARENT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD))
        val rootJavaType = ROOT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD))
        val javaType = TypeSpec.classBuilder(rootProjectionClass)
            .addOptionalGeneratedAnnotation(config)
            .addTypeVariable(parentJavaType)
            .addTypeVariable(rootJavaType)
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, PARENT, ROOT))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super(null, null, \$T.of(\$S))", OPTIONAL, type.name)
                    .build()
            )

        javaType.addMethod(
            MethodSpec.methodBuilder(TypeNameMetaFieldDef.name)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(rootProjectionClass, PARENT, ROOT))
                .addStatement("getFields().put(\$S, null)", TypeNameMetaFieldDef.name)
                .addStatement("return this")
                .build()
        )

        if (!generatedClasses.add(rootProjectionClass.simpleName())) {
            return CodeGenResult.EMPTY
        }

        val fieldDefinitions = type.fieldDefinitions().asSequence() + document.definitions.asSequence()
            .filterIsInstance<ObjectTypeExtensionDefinition>()
            .filter { it.name == type.name }
            .flatMap { it.fieldDefinitions }

        val codeGenResult = fieldDefinitions
            .filterSkipped()
            .mapNotNull {
                val typeDefinition = it.type.findTypeDefinition(
                    document = document,
                    excludeExtensions = true,
                    includeBaseTypes = it.inputValueDefinitions.isNotEmpty(),
                    includeScalarTypes = it.inputValueDefinitions.isNotEmpty()
                )
                if (typeDefinition != null) it to typeDefinition else null
            }
            .map { (fieldDef, typeDef) ->
                val projectionClass = ClassName.get(getPackageName(), "${typeDef.name.capitalized()}Projection")
                if (typeDef !is ScalarTypeDefinition) {
                    val projectionType = ParameterizedTypeName.get(
                        projectionClass,
                        ParameterizedTypeName.get(rootProjectionClass, PARENT, ROOT),
                        ParameterizedTypeName.get(rootProjectionClass, PARENT, ROOT)
                    )

                    val noArgMethodBuilder = MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(fieldDef.name))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(projectionType)
                        .addStatement("\$T projection = new \$T<>(this, this)", projectionType, projectionType.rawType)
                        .addStatement("getFields().put(\$S, projection)", fieldDef.name)
                        .addStatement("return projection")
                    javaType.addMethod(noArgMethodBuilder.build())
                }

                if (fieldDef.inputValueDefinitions.isNotEmpty()) {
                    addFieldSelectionMethodWithArguments(
                        fieldDefinition = fieldDef,
                        projectionClass = projectionClass,
                        javaType = javaType,
                        projectionRoot = "this"
                    )
                }

                val processedEdges = mutableSetOf<Pair<String, String>>()
                processedEdges += typeDef.name to type.name
                createSubProjection(
                    typeDef,
                    javaType.build(),
                    typeDef.name.capitalized(),
                    processedEdges,
                    1
                )
            }
            .fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {
            val objectTypeDefinition = it.type.findTypeDefinition(document)
            if (objectTypeDefinition == null) {
                javaType.addMethod(
                    MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(rootProjectionClass, PARENT, ROOT))
                        .addStatement("getFields().put(\$S, null)", it.name)
                        .addStatement("return this")
                        .build()
                )
            }
        }

        val concreteTypesResult = createConcreteTypes(type, javaType.build(), javaType, mutableSetOf())
        val unionTypesResult = createUnionTypes(type, javaType, javaType.build(), mutableSetOf())

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult).merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun addFieldSelectionMethodWithArguments(
        fieldDefinition: FieldDefinition,
        projectionClass: ClassName,
        javaType: TypeSpec.Builder,
        projectionRoot: String
    ): TypeSpec.Builder? {
        val clazzName = ClassName.get(getPackageName(), javaType.build().name)
        val projectionType = ParameterizedTypeName.get(
            projectionClass,
            ParameterizedTypeName.get(clazzName, PARENT, ROOT),
            if (projectionRoot == "this") ParameterizedTypeName.get(clazzName, PARENT, ROOT) else ROOT
        )

        val methodBuilder = MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(fieldDefinition.name))
            .addModifiers(Modifier.PUBLIC)
            .returns(projectionType)
            .addStatement("\$T projection = new \$T(this, \$L)", projectionType, projectionType.rawType, projectionRoot)
            .addStatement("getFields().put(\$S, projection)", fieldDefinition.name)
            .addStatement("getInputArguments().computeIfAbsent(\$S, k -> new \$T<>())", fieldDefinition.name, ARRAYLIST)
            .addCode(
                CodeBlock.join(
                    fieldDefinition.inputValueDefinitions.map { input ->
                        CodeBlock.builder()
                            .addStatement("InputArgument \$LArg = new InputArgument(\$S, \$L)", input.name, input.name, input.name)
                            .addStatement("getInputArguments().get(\$S).add(\$LArg)", fieldDefinition.name, input.name)
                            .build()
                    },
                    "\n"
                )
            )
            .addStatement("return projection")

        fieldDefinition.inputValueDefinitions.forEach { input ->
            methodBuilder.addParameter(ParameterSpec.builder(typeUtils.findReturnType(input.type), input.name).build())
        }
        return javaType.addMethod(methodBuilder.build())
    }

    private fun createEntitiesRootProjection(federatedTypes: List<ObjectTypeDefinition>): CodeGenResult {
        val clazzName = ClassName.get(getPackageName(), "EntitiesProjectionRoot")
        val parentType = PARENT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD))
        val rootType = ROOT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD))
        val javaType = TypeSpec.classBuilder(clazzName)
            .addOptionalGeneratedAnnotation(config)
            .addTypeVariable(parentType)
            .addTypeVariable(rootType)
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, PARENT, ROOT))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("super(null, null, \$T.of(\$S))", OPTIONAL, "_entities")
                    .build()
            )

        if (!generatedClasses.add(clazzName.simpleName())) {
            return CodeGenResult.EMPTY
        }

        val codeGenResult = federatedTypes.map { objTypeDef ->
            val projectionName = ClassName.get(getPackageName(), "Entities${objTypeDef.name.capitalized()}KeyProjection")
            val returnType = ParameterizedTypeName.get(
                projectionName,
                ParameterizedTypeName.get(clazzName, PARENT, ROOT),
                ParameterizedTypeName.get(clazzName, PARENT, ROOT)
            )

            javaType.addMethod(
                MethodSpec.methodBuilder("on${objTypeDef.name}")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(returnType)
                    .addStatement("\$T fragment = new \$T<>(this, this)", returnType, returnType.rawType)
                    .addStatement("getFragments().add(fragment)")
                    .addStatement("return fragment")
                    .build()
            )
            createFragment(
                objTypeDef,
                javaType.build(),
                "Entities${objTypeDef.name.capitalized()}Key"
            )
        }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createConcreteTypes(
        type: TypeDefinition<*>,
        root: TypeSpec,
        javaType: TypeSpec.Builder,
        processedEdges: MutableSet<Pair<String, String>>,
        queryDepth: Int = 0
    ): CodeGenResult {
        if (type !is InterfaceTypeDefinition) {
            return CodeGenResult.EMPTY
        }
        val concreteTypes = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).asSequence().filter {
            it.implements.asSequence().filterIsInstance<NamedNode<*>>().any { iface -> iface.name == type.name }
        }
        return concreteTypes.map {
            addFragmentProjectionMethod(javaType, root, it, processedEdges, queryDepth)
        }.fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }
    }

    private fun createUnionTypes(
        type: TypeDefinition<*>,
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
        processedEdges: MutableSet<Pair<String, String>>,
        queryDepth: Int = 0
    ): CodeGenResult {
        if (type !is UnionTypeDefinition) {
            return CodeGenResult.EMPTY
        }

        var result = CodeGenResult.EMPTY
        for (memberType in type.memberTypes) {
            val typeDefinition = memberType.findTypeDefinition(document = document, excludeExtensions = true)
                ?: continue
            result = result.merge(
                addFragmentProjectionMethod(javaType, rootType, typeDefinition, processedEdges, queryDepth)
            )
        }
        return result
    }

    private fun addFragmentProjectionMethod(
        javaType: TypeSpec.Builder,
        rootType: TypeSpec,
        typeDefinition: TypeDefinition<*>,
        processedEdges: MutableSet<Pair<String, String>>,
        queryDepth: Int = 0
    ): CodeGenResult {
        val rootRef = if (javaType.build().name == rootType.name) "this" else "getRoot()"

        val rootClass = ClassName.get(getPackageName(), rootType.name)
        val parentRef = ClassName.get(getPackageName(), javaType.build().name)
        val projectionName = "${typeDefinition.name.capitalized()}Fragment"
        val fullProjectionName = ClassName.get(getPackageName(), "${projectionName}Projection")
        val projectionClass = ParameterizedTypeName.get(
            fullProjectionName,
            ParameterizedTypeName.get(parentRef, PARENT, ROOT),
            if (javaType.build().name == rootType.name) ParameterizedTypeName.get(rootClass, PARENT, ROOT) else ROOT
        )

        javaType.addMethod(
            MethodSpec.methodBuilder("on${typeDefinition.name}")
                .addModifiers(Modifier.PUBLIC)
                .returns(projectionClass)
                .addStatement("\$T fragment = new \$T<>(this, \$L)", projectionClass, projectionClass.rawType, rootRef)
                .addStatement("getFragments().add(fragment)")
                .addStatement("return fragment")
                .build()
        )

        return createFragment(typeDefinition as ObjectTypeDefinition, rootType, projectionName, processedEdges, queryDepth)
    }

    private fun createFragment(
        type: ObjectTypeDefinition,
        root: TypeSpec,
        prefix: String,
        processedEdges: MutableSet<Pair<String, String>> = mutableSetOf(),
        queryDepth: Int = 0
    ): CodeGenResult {
        val subProjection = createSubProjectionType(type, root, prefix, processedEdges, queryDepth)
            ?: return CodeGenResult.EMPTY
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        // We don't need the typename added for fragments in the entities' projection.
        // This affects deserialization when use directly with generated classes
        if (prefix != "Entities${type.name.capitalized()}Key") {
            javaType.addInitializerBlock(
                CodeBlock.builder()
                    .addStatement("getFields().put(\$S, null)", TypeNameMetaFieldDef.name)
                    .build()
            )
        }

        javaType.addMethod(
            MethodSpec.methodBuilder("toString")
                .returns(STRING)
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("\$T builder = new \$T()", ClassName.get(StringBuilder::class.java), ClassName.get(StringBuilder::class.java))
                .addStatement("builder.append(\$S)", "... on ${type.name} {")
                .beginControlFlow(
                    "for (\$T entry : getFields().entrySet())",
                    ParameterizedTypeName.get(ClassName.get(Map.Entry::class.java), STRING, ClassName.OBJECT)
                )
                .addStatement("""builder.append(" ").append(entry.getKey())""")
                .beginControlFlow("if (entry.getValue() != null)")
                .addStatement("""builder.append(" ").append(entry.getValue())""")
                .endControlFlow()
                .endControlFlow()
                .addStatement("""builder.append("}")""")
                .addStatement("return builder.toString()")
                .build()
        )

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjection(
        type: TypeDefinition<*>,
        root: TypeSpec,
        prefix: String,
        processedEdges: MutableSet<Pair<String, String>>,
        queryDepth: Int = 0
    ): CodeGenResult {
        val subProjection = createSubProjectionType(type, root, prefix, processedEdges, queryDepth)
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
        processedEdges: MutableSet<Pair<String, String>>,
        queryDepth: Int = 0
    ): Pair<TypeSpec.Builder, CodeGenResult>? {
        val clazzName = ClassName.get(getPackageName(), "${prefix}Projection")
        if (!generatedClasses.add(clazzName.simpleName())) {
            return null
        }

        val javaType = TypeSpec.classBuilder(clazzName)
            .addOptionalGeneratedAnnotation(config)
            .addTypeVariable(PARENT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD)))
            .addTypeVariable(ROOT.withBounds(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, WILDCARD, WILDCARD)))
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(BASE_SUB_PROJECTION_NODE, PARENT, ROOT))
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(PARENT, "parent")
                    .addParameter(ROOT, "root")
                    .addStatement("super(parent, root, \$T.of(\$S))", ClassName.get(Optional::class.java), type.name)
                    .build()
            )

        // add a method for setting the __typename
        javaType.addMethod(
            MethodSpec.methodBuilder(TypeNameMetaFieldDef.name)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(clazzName, PARENT, ROOT))
                .addStatement("getFields().put(\$S, null)", TypeNameMetaFieldDef.name)
                .addStatement("return this")
                .build()
        )

        val fieldDefinitions = (
            type.fieldDefinitions().asSequence() +
                document.definitions.asSequence()
                    .filterIsInstance<ObjectTypeExtensionDefinition>()
                    .filter { it.name == type.name }
                    .flatMap { it.fieldDefinitions }
            ).toList()

        val codeGenResult =
            fieldDefinitions
                .asSequence()
                .filterSkipped()
                .mapNotNull {
                    val typeDefinition = it.type.findTypeDefinition(document, true)
                    if (typeDefinition != null) it to typeDefinition else null
                }
                .map { (fieldDef, typeDef) ->
                    val projectionName = ClassName.get(getPackageName(), "${typeDef.name.capitalized()}Projection")
                    val methodName = ReservedKeywordSanitizer.sanitize(fieldDef.name)
                    val projectionType = ParameterizedTypeName.get(
                        projectionName,
                        ParameterizedTypeName.get(clazzName, PARENT, ROOT),
                        ROOT
                    )

                    javaType.addMethod(
                        MethodSpec.methodBuilder(methodName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(projectionType)
                            .addStatement("\$T projection = new \$T<>(this, getRoot())", projectionType, projectionType.rawType)
                            .addStatement("getFields().put(\$S, projection)", fieldDef.name)
                            .addStatement("return projection")
                            .build()
                    )

                    if (fieldDef.inputValueDefinitions.isNotEmpty()) {
                        addFieldSelectionMethodWithArguments(fieldDef, projectionName, javaType, projectionRoot = "getRoot()")
                    }

                    processedEdges += typeDef.name to type.name
                    createSubProjection(
                        typeDef,
                        root,
                        typeDef.name.capitalized(),
                        processedEdges,
                        queryDepth + 1
                    )
                }
                .fold(CodeGenResult.EMPTY) { total, current -> total.merge(current) }

        fieldDefinitions
            .asSequence()
            .filterSkipped()
            .forEach {
                val objectTypeDefinition = it.type.findTypeDefinition(document)
                if (objectTypeDefinition == null) {
                    javaType.addMethod(
                        MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                            .returns(ParameterizedTypeName.get(clazzName, PARENT, ROOT))
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("getFields().put(\$S, null)", it.name)
                            .addStatement("return this")
                            .build()
                    )

                    if (it.inputValueDefinitions.isNotEmpty()) {
                        val methodWithInputArgumentsBuilder = MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                            .addModifiers(Modifier.PUBLIC)
                            .returns(ClassName.get(getPackageName(), javaType.build().name))
                            .addStatement("getFields().put(\$S, null)", it.name)
                            .addStatement("getInputArguments().computeIfAbsent(\$S, k -> new \$T<>())", it.name, ARRAYLIST)
                            .addCode(
                                CodeBlock.join(
                                    it.inputValueDefinitions.map { input ->
                                        CodeBlock.builder()
                                            .addStatement("InputArgument \$LArg = new InputArgument(\$S, \$L)", input.name, input.name, input.name)
                                            .addStatement("getInputArguments().get(\$S).add(\$LArg)", it.name, input.name)
                                            .build()
                                    },
                                    "\n"
                                )
                            )
                            .addStatement("return this")

                        it.inputValueDefinitions.forEach { input ->
                            methodWithInputArgumentsBuilder.addParameter(ParameterSpec.builder(typeUtils.findReturnType(input.type), input.name).build())
                        }

                        javaType.addMethod(methodWithInputArgumentsBuilder.build())
                    }
                }
            }

        val concreteTypesResult = createConcreteTypes(type, root, javaType, processedEdges, queryDepth)
        val unionTypesResult = createUnionTypes(type, javaType, root, processedEdges, queryDepth)

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

    private fun getDeprecatedReason(directive: Directive): String? {
        return directive.getArgument("reason")
            ?.let { it.value as? StringValue }
            ?.value
    }

    private fun getPackageName(): String {
        return config.packageNameClient
    }

    private fun getDatatypesPackageName(): String {
        return config.packageNameTypes
    }
}
