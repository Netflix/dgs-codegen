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

package com.netflix.graphql.dgs.codegen.generators.kotlin

import com.netflix.graphql.dgs.client.codegen.BaseProjectionNode
import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery
import com.netflix.graphql.dgs.codegen.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.*

class KotlinClientApiGenerator(private val config: CodeGenConfig, private val document: Document) {
    private val generatedClasses = mutableSetOf<String>()

    fun generate(definition: ObjectTypeDefinition): KotlinCodeGenResult {

        return definition.fieldDefinitions.filterIncludedInConfig(definition.name, config).filterSkipped().map {
            val javaFile = createQueryClass(it, definition.name)

            val rootProjection = it.type.findTypeDefinition(document, true)?.let { typeDefinition -> createRootProjection(typeDefinition, it.name.capitalize()) }
                    ?: KotlinCodeGenResult()
            KotlinCodeGenResult(queryTypes = listOf(javaFile)).merge(rootProjection)
        }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }
    }

    fun generateEntities(definitions: List<ObjectTypeDefinition>): KotlinCodeGenResult {
        if(config.skipEntityQueries) {
            return KotlinCodeGenResult()
        }

        var entitiesRootProjection = KotlinCodeGenResult()
        // generate for federation types, if present
        val federatedTypes = definitions.filter { it.getDirective("key") != null }
        if (federatedTypes.isNotEmpty()) {
            // create entities root projection
            entitiesRootProjection = createEntitiesRootProjection(federatedTypes)
        }
        return KotlinCodeGenResult().merge(entitiesRootProjection)
    }

    private fun createQueryClass(it: FieldDefinition, operation: String): FileSpec {
        val javaType = TypeSpec.classBuilder("${it.name.capitalize()}GraphQLQuery")
                .superclass(GraphQLQuery::class.asTypeName())
                .addSuperclassConstructorParameter("%S", operation.toLowerCase())

        if (it.inputValueDefinitions.isNotEmpty()) {
            javaType.addModifiers(KModifier.DATA)
        }

        javaType.addFunction(
                FunSpec.builder("getOperationName")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(String::class)
                        .addCode("""
                                    return "${it.name}";
                                    
                                    """.trimIndent()).build())

        val builderClass = TypeSpec.classBuilder("Builder")
                .addFunction(FunSpec.builder("build")
                        .addModifiers(KModifier.PUBLIC)
                        .returns(ClassName.bestGuess("${it.name.capitalize()}GraphQLQuery"))
                        .addCode("""
                                     return ${it.name.capitalize()}GraphQLQuery(${it.inputValueDefinitions.joinToString(", ") { it.name }})
                                     
                                """.trimIndent())
                        .build())

        val constructorBuilder = FunSpec.constructorBuilder()

        it.inputValueDefinitions.forEach { inputValue ->
            val findReturnType = KotlinTypeUtils(getTypesPackageName(), config).findReturnType(inputValue.type)
            builderClass
                    .addFunction(FunSpec.builder(inputValue.name)
                            .addParameter(inputValue.name, findReturnType)
                            .returns(ClassName.bestGuess("Builder"))
                            .addCode("""
                                this.${inputValue.name} = ${inputValue.name};
                                return this
                            """.trimIndent()).build())

            constructorBuilder.addParameter(ParameterSpec.builder(inputValue.name, findReturnType.copy(nullable = true)).defaultValue(getInitializer(findReturnType)).build())
            constructorBuilder.addCode("""
                if(${inputValue.name} != null) {
                    input["${inputValue.name}"] = ${inputValue.name}
                }
                
                """.trimIndent())
            javaType.addProperty(PropertySpec.builder(inputValue.name, findReturnType.copy(nullable = true)).initializer(inputValue.name).build())
            builderClass.addProperty(PropertySpec.builder(inputValue.name, findReturnType.copy(nullable = true)).mutable().initializer(getInitializer(findReturnType)).build())
        }

        javaType.primaryConstructor(constructorBuilder.build())

        javaType.addType(builderClass.build())


        val typeSpec = javaType.build()
        return FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
    }

    private fun getInitializer(findReturnType: com.squareup.kotlinpoet.TypeName): String {
        return when (findReturnType) {
            INT -> "0"
            DOUBLE -> "0"
            LONG -> "0"
            else -> "null"
        }
    }

    private fun createRootProjection(type: TypeDefinition<*>, prefix: String): KotlinCodeGenResult {

        val clazzName = "${prefix}ProjectionRoot"
        if(generatedClasses.contains(clazzName)) return KotlinCodeGenResult() else generatedClasses.add(clazzName)

        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(KModifier.PUBLIC)
                .superclass(BaseProjectionNode::class.asTypeName())

        val fieldDefinitions = type.fieldDefinitions() + document.definitions.filterIsInstance<ObjectTypeExtensionDefinition>().filter { it.name == type.name}.flatMap { it.fieldDefinitions }
        val codeGenResult = fieldDefinitions.filterSkipped()
                .mapNotNull { if (it.type.findTypeDefinition(document) != null ) Pair(it, it.type.findTypeDefinition(document)) else null }
                .map {
                    val projectionName = "${prefix}${it.first.name.capitalize()}Projection"
                    javaType.addFunction(FunSpec.builder(it.first.name)
                            .returns(ClassName.bestGuess("${getPackageName()}.${projectionName}"))
                            .addCode("""
                        val projection = ${projectionName}(this, this)    
                        fields["${it.first.name}"] = projection
                        return projection
                    """.trimIndent())
                            .addModifiers(KModifier.PUBLIC)
                            .build())
                    val processedEdges = mutableSetOf<Pair<String, String>>()
                    processedEdges.add(Pair(it.second!!.name, type.name))
                    createSubProjection(it.second!!, javaType.build(), javaType.build(), "${prefix}${it.first.name.capitalize()}", processedEdges)
                }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {
            val objectTypeDefinition = it.type.findTypeDefinition(document)
            if (objectTypeDefinition == null) {
                javaType.addFunction(FunSpec.builder(it.name)
                        .returns(ClassName.bestGuess("${getPackageName()}.${javaType.build().name}"))
                        .addCode("""
                        fields["${it.name}"] = null
                        return this
                    """.trimIndent())
                        .addModifiers(KModifier.PUBLIC)
                        .build())
            }
        }

        val concreteTypesResult = createConcreteTypes(type, javaType, javaType.build(), prefix, mutableSetOf<Pair<String, String>>())
        val unionTypesResult = createUnionTypes(type, javaType, javaType.build(), prefix, mutableSetOf<Pair<String, String>>())

        val typeSpec = javaType.build()
        val javaFile = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult).merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun createEntitiesRootProjection(federatedTypes: List<ObjectTypeDefinition>): KotlinCodeGenResult {
        val clazzName = "EntitiesProjectionRoot"

        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(KModifier.PUBLIC)
                .superclass(BaseProjectionNode::class.asTypeName())

        if(generatedClasses.contains(clazzName)) return KotlinCodeGenResult() else generatedClasses.add(clazzName)
        val codeGenResult = federatedTypes
                .map {
                    javaType.addFunction(FunSpec.builder("on${it.name}")
                            .addModifiers(KModifier.PUBLIC)
                            .returns(ClassName.bestGuess("${getPackageName()}.Entities${it.name.capitalize()}KeyProjection"))
                            .addCode("""
                                val fragment = Entities${it.name.capitalize()}KeyProjection(this, this)
                                fragments.add(fragment)
                                return fragment
                            """.trimIndent())
                            .build())
                    val processedEdges = mutableSetOf<Pair<String, String>>()
                    createFragment(it, javaType.build(), javaType.build(), "Entities${it.name.capitalize()}Key", processedEdges)
                }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }

        val typeSpec = javaType.build()
        val javaFile = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createConcreteTypes(type: TypeDefinition<*>, javaType: TypeSpec.Builder, rootType: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): KotlinCodeGenResult {
        return if (type is InterfaceTypeDefinition) {
            val concreteTypes = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).filter { it.implements.filterIsInstance<NamedNode<*>>().find { iface -> iface.name == type.name } != null }
            concreteTypes.map {
                javaType.addFunction(FunSpec.builder("on${it.name}")
                        .returns(ClassName.bestGuess("${getPackageName()}.${prefix}${it.name.capitalize()}Projection"))
                        .addCode("""
                                val fragment = ${prefix}${it.name.capitalize()}Projection(this, root)
                                fragments.add(fragment)
                                return fragment;
                            """.trimIndent())
                        .build())

                createFragment(it, javaType.build(), rootType, "${prefix}${it.name.capitalize()}", processedEdges)
            }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }
        } else KotlinCodeGenResult()
    }

    private fun createUnionTypes(type: TypeDefinition<*>, javaType: TypeSpec.Builder, rootType: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): KotlinCodeGenResult {
        return if (type is UnionTypeDefinition) {
            val memberTypes = type.memberTypes.mapNotNull { it.findTypeDefinition(document) }

            memberTypes.map {
                javaType.addFunction(FunSpec.builder("on${it.name}")
                        .returns(ClassName.bestGuess("${getPackageName()}.${prefix}${it.name.capitalize()}Projection"))
                        .addCode("""
                                val fragment = ${prefix}${it.name.capitalize()}Projection(this, root)
                                fragments.add(fragment)
                                return fragment;
                            """.trimIndent())
                        .build())

                createFragment(it as ObjectTypeDefinition, javaType.build(), rootType, "${prefix}${it.name.capitalize()}", processedEdges)
            }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }
        } else KotlinCodeGenResult()
    }

    private fun createFragment(type: ObjectTypeDefinition, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): KotlinCodeGenResult {
        val subProjection = createSubProjectionType(type, parent, root, prefix, processedEdges) ?: return KotlinCodeGenResult()
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        javaType.addInitializerBlock(CodeBlock.builder()
                .addStatement("fields[%S] = null", TypeNameMetaFieldDef.name)
                .build())

        javaType.addFunction(FunSpec.builder("toString")
                .returns(STRING)
                .addModifiers(KModifier.OVERRIDE)
                .addCode("""
                    val builder = StringBuilder()
                    builder.append("... on ${type.name} {")
                    fields.forEach { k, v ->
                        builder.append(" ").append(k)
                        if(v != null) {
                            builder.append(" ").append(v.toString())
                        }
                    }
                    builder.append("}")
            
                    return builder.toString()
                """.trimIndent())
                .build())

        val typeSpec = javaType.build()
        val javaFile = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjection(type: TypeDefinition<*>, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): KotlinCodeGenResult {
        val (javaType, codeGenResult) = createSubProjectionType(type, parent, root, prefix, processedEdges) ?: return KotlinCodeGenResult()

        val typeSpec = javaType.build()
        val javaFile = FileSpec.builder(getPackageName(), typeSpec.name!!).addType(typeSpec).build()
        return KotlinCodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjectionType(type: TypeDefinition<*>, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>) : Pair<TypeSpec.Builder, KotlinCodeGenResult>? {
        val className = ClassName(BaseSubProjectionNode::class.java.`package`.name, BaseSubProjectionNode::class.java.simpleName)
        val clazzName = "${prefix}Projection"

        if (generatedClasses.contains(clazzName)) return null else generatedClasses.add(clazzName)

        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(KModifier.PUBLIC)
                .superclass(className.parameterizedBy(ClassName.bestGuess("${getPackageName()}.${parent.name}"), ClassName.bestGuess("${getPackageName()}.${root.name}")))
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("parent", ClassName.bestGuess("${getPackageName()}.${parent.name}"))
                        .addParameter("root", ClassName.bestGuess("${getPackageName()}.${root.name}"))

                        .build())
                .addSuperclassConstructorParameter("parent")
                .addSuperclassConstructorParameter("root")

        val fieldDefinitions = type.filterInterfaceFields(document) + document.definitions.filterIsInstance<ObjectTypeExtensionDefinition>().filter { it.name == type.name}.flatMap { it.fieldDefinitions }
        val codeGenResult = fieldDefinitions.filterSkipped()
                .mapNotNull { if (it.type.findTypeDefinition(document) != null) Pair(it, it.type.findTypeDefinition(document)) else null }
                .filter { ! processedEdges.contains(Pair(it.second!!.name, type.name)) }
                .map {
                    val projectionName = "${prefix}${it.first.name.capitalize()}Projection"
                    javaType.addFunction(FunSpec.builder(it.first.name)
                            .returns(ClassName.bestGuess("${getPackageName()}.${projectionName}"))
                            .addCode("""
                        val projection = ${projectionName}(this, root)    
                        fields["${it.first.name}"] = projection
                        return projection
                    """.trimIndent())
                            .addModifiers(KModifier.PUBLIC)
                            .build())
                    val updatedProcessedEdges = processedEdges.toMutableSet()
                    updatedProcessedEdges.add(Pair(it.second!!.name, type.name))
                    createSubProjection(it.second!!, javaType.build(), root, "${prefix}${it.first.name.capitalize()}", updatedProcessedEdges)
                }.fold(KotlinCodeGenResult()) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {

            val objectTypeDefinition = it.type.findTypeDefinition(document)
            if (objectTypeDefinition == null) {
                javaType.addFunction(FunSpec.builder(it.name)
                        .returns(ClassName.bestGuess("${getPackageName()}.${javaType.build().name}"))
                        .addCode("""
                        fields["${it.name}"] = null
                        return this
                    """.trimIndent())
                        .addModifiers(KModifier.PUBLIC)
                        .build())
            }
        }

        val concreteTypesResult = createConcreteTypes(type, javaType, root, prefix, processedEdges)
        val unionTypesResult = createUnionTypes(type, javaType, root, prefix, processedEdges)

        return Pair(javaType, codeGenResult.merge(concreteTypesResult).merge(unionTypesResult))
    }

    fun getPackageName(): String {
        return config.packageName + ".client"
    }

    private fun getTypesPackageName(): String {
        return config.packageName + ".types"
    }
}