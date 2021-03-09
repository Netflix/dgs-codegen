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

import com.netflix.graphql.dgs.client.codegen.BaseProjectionNode
import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery
import com.netflix.graphql.dgs.codegen.*
import com.netflix.graphql.dgs.codegen.generators.shared.ClassnameShortener
import com.squareup.javapoet.*
import graphql.introspection.Introspection.TypeNameMetaFieldDef
import graphql.language.*
import javax.lang.model.element.Modifier

class ClientApiGenerator(private val config: CodeGenConfig, private val document: Document) {
    private val generatedClasses = mutableSetOf<String>()
    private val projectionDepth = mutableMapOf<String, Int>()

    fun generate(definition: ObjectTypeDefinition): CodeGenResult {
        return definition.fieldDefinitions.filterIncludedInConfig(definition.name, config).filterSkipped().map {
            val javaFile = createQueryClass(it, definition.name)

            val rootProjection = it.type.findTypeDefinition(document, true)?.let { typeDefinition -> createRootProjection(typeDefinition, it.name.capitalize()) } ?: CodeGenResult()
            CodeGenResult(queryTypes = listOf(javaFile)).merge(rootProjection)
        }.fold(CodeGenResult()) { total, current -> total.merge(current) }
    }

    fun generateEntities(definitions: List<ObjectTypeDefinition>): CodeGenResult {
            if(config.skipEntityQueries) {
                return CodeGenResult()
            }

            var entitiesRootProjection = CodeGenResult()
            // generate for federation types, if present
            val federatedTypes = definitions.filter { it.hasDirective("key") }
            if (federatedTypes.isNotEmpty()) {
                // create entities root projection
                entitiesRootProjection = createEntitiesRootProjection(federatedTypes)
            }
            return CodeGenResult().merge(entitiesRootProjection)
    }

    private fun createQueryClass(it: FieldDefinition, operation: String): JavaFile {
        val javaType = TypeSpec.classBuilder("${it.name.capitalize()}GraphQLQuery")
                .addModifiers(Modifier.PUBLIC).superclass(ClassName.get(GraphQLQuery::class.java))
        println("Generating ${it.name.capitalize()}GraphQLQuery")
        javaType.addMethod(
                MethodSpec.methodBuilder("getOperationName")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String::class.java)
                        .addAnnotation(Override::class.java)
                        .addCode("""
                                    return "${it.name}";
                                    
                                    """.trimIndent()).build())

        val builderClass = TypeSpec.classBuilder("Builder").addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("", "${it.name.capitalize()}GraphQLQuery"))
                        .addCode("""
                                     return new ${it.name.capitalize()}GraphQLQuery(${it.inputValueDefinitions.joinToString(", ") { ReservedKeywordSanitizer.sanitize(it.name) }});
                                     
                                """.trimIndent())
                        .build())

        val constructorBuilder = MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PUBLIC)
        constructorBuilder.addCode("""
                super("${operation.toLowerCase()}");
                
            """.trimIndent())

        it.inputValueDefinitions.forEach { inputValue ->
            val findReturnType = TypeUtils(getDatatypesPackageName(), config, document).findReturnType(inputValue.type)
            builderClass
                    .addMethod(MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(inputValue.name))
                            .addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))
                            .returns(ClassName.get("", "Builder"))
                            .addModifiers(Modifier.PUBLIC)
                            .addCode("""
                                this.${ReservedKeywordSanitizer.sanitize(inputValue.name)} = ${ReservedKeywordSanitizer.sanitize(inputValue.name)};
                                return this;
                            """.trimIndent()).build())
                    .addField(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name), Modifier.PRIVATE)

            constructorBuilder.addParameter(findReturnType, ReservedKeywordSanitizer.sanitize(inputValue.name))
            constructorBuilder.addCode("""
                if (${inputValue.name} != null) {
                    getInput().put("${inputValue.name}", ${ReservedKeywordSanitizer.sanitize(inputValue.name)});
                }
            """.trimIndent())
        }

        javaType.addMethod(constructorBuilder.build())

        //No-arg constructor
        if (it.inputValueDefinitions.size > 0) {
            javaType.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
                    .addStatement("super(\"${operation.toLowerCase()}\")")
                    .build())
        }

        javaType.addMethod(MethodSpec.methodBuilder("newRequest")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(ClassName.get("", "Builder"))
                .addCode("return new Builder();\n")
                .build())
        javaType.addType(builderClass.build())
        return JavaFile.builder(getPackageName(), javaType.build()).build()
    }

    private fun createRootProjection(type: TypeDefinition<*>, prefix: String): CodeGenResult {
        val clazzName = "${prefix}ProjectionRoot"
        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC).superclass(ClassName.get(BaseProjectionNode::class.java))
        println("Generating ${prefix}ProjectionRoot")

        if(generatedClasses.contains(clazzName)) return CodeGenResult() else generatedClasses.add(clazzName)

        val fieldDefinitions = type.fieldDefinitions() + document.definitions.filterIsInstance<ObjectTypeExtensionDefinition>().filter { it.name == type.name}.flatMap { it.fieldDefinitions }
        val codeGenResult = fieldDefinitions.filterSkipped()
                .mapNotNull { if (it.type.findTypeDefinition(document) != null ) Pair(it, it.type.findTypeDefinition(document)) else null }
                .map {
                    val projectionName = "${prefix}${it.first.name.capitalize()}Projection"
                    javaType.addMethod(MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.first.name))
                            .returns(ClassName.get(getPackageName(), projectionName))
                            .addCode("""
                        $projectionName projection = new $projectionName(this, this);    
                        getFields().put("${it.first.name}", projection);
                        return projection;
                    """.trimIndent())
                            .addModifiers(Modifier.PUBLIC)
                            .build())
                    val processedEdges = mutableSetOf<Pair<String, String>>()
                    processedEdges.add(Pair(it.second!!.name, type.name))
                    createSubProjection(it.second!!, javaType.build(), javaType.build(), "${prefix}${it.first.name.capitalize()}", processedEdges)
                }
                .fold(CodeGenResult()) { total, current -> total.merge(current) }

        fieldDefinitions.filterSkipped().forEach {

            val objectTypeDefinition = it.type.findTypeDefinition(document)
            if (objectTypeDefinition == null) {
                javaType.addMethod(MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                        .returns(ClassName.get(getPackageName(), javaType.build().name))
                        .addCode("""
                        getFields().put("${it.name}", null);
                        return this;
                    """.trimIndent())
                        .addModifiers(Modifier.PUBLIC)
                        .build())
            }
        }

        val concreteTypesResult = createConcreteTypes(type, javaType.build(), javaType, prefix, mutableSetOf<Pair<String, String>>())
        val unionTypesResult = createUnionTypes(type, javaType, javaType.build(), prefix, mutableSetOf<Pair<String, String>>())

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult).merge(concreteTypesResult).merge(unionTypesResult)
    }

    private fun createEntitiesRootProjection(federatedTypes: List<ObjectTypeDefinition>): CodeGenResult {
        val clazzName = "EntitiesProjectionRoot"
        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC).superclass(ClassName.get(BaseProjectionNode::class.java))

        if(generatedClasses.contains(clazzName)) return CodeGenResult() else generatedClasses.add(clazzName)
        val codeGenResult = federatedTypes
                .map {
            javaType.addMethod(MethodSpec.methodBuilder("on${it.name}")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(getPackageName(), "Entities${it.name.capitalize()}KeyProjection"))
                    .addCode("""
                                Entities${it.name.capitalize()}KeyProjection fragment = new Entities${it.name.capitalize()}KeyProjection(this, this);
                                getFragments().add(fragment);
                                return fragment;
                            """.trimIndent())
                    .build())
                    val processedEdges = mutableSetOf<Pair<String, String>>()
            createFragment(it, javaType.build(), javaType.build(), "Entities${it.name.capitalize()}Key", processedEdges)
        }.fold(CodeGenResult()) { total, current -> total.merge(current) }

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createConcreteTypes(type: TypeDefinition<*>, root: TypeSpec, javaType: TypeSpec.Builder, prefix: String, processedEdges: Set<Pair<String, String>>): CodeGenResult {
        return if (type is InterfaceTypeDefinition) {

            val concreteTypes = document.getDefinitionsOfType(ObjectTypeDefinition::class.java).filter {
                it.implements.filterIsInstance<NamedNode<*>>().find { iface -> iface.name == type.name } != null }
            concreteTypes.map {
                addFragmentProjectionMethod(javaType, root, prefix, it, processedEdges)
            }.fold(CodeGenResult()) { total, current -> total.merge(current) }
        } else {
            CodeGenResult()
        }
    }

    private fun createUnionTypes(type: TypeDefinition<*>, javaType: TypeSpec.Builder, rootType: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): CodeGenResult {
        return if (type is UnionTypeDefinition) {
            val memberTypes = type.memberTypes.mapNotNull { it.findTypeDefinition(document) }.toList()
            memberTypes.map {
                addFragmentProjectionMethod(javaType, rootType, prefix, it, processedEdges)
            }.fold(CodeGenResult()) { total, current -> total.merge(current) }
        } else {
            CodeGenResult()
        }
    }

    private fun addFragmentProjectionMethod(javaType: TypeSpec.Builder, rootType: TypeSpec, prefix: String, it: TypeDefinition<*>, processedEdges: Set<Pair<String, String>>): CodeGenResult {
        val rootRef = if (javaType.build().name == rootType.name) "this" else "getRoot()"

        val projectionName = "${prefix}${it.name.capitalize()}Projection"
        javaType.addMethod(MethodSpec.methodBuilder("on${it.name}")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(getPackageName(), projectionName))
                .addCode("""
                                    $projectionName fragment = new ${projectionName}(this, ${rootRef});
                                    getFragments().add(fragment);
                                    return fragment;
                                """.trimIndent())
                .build())

        return createFragment(it as ObjectTypeDefinition, javaType.build(), rootType, "${prefix}${it.name.capitalize()}", processedEdges)
    }

    private fun createFragment(type: ObjectTypeDefinition, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): CodeGenResult {
        val subProjection = createSubProjectionType(type, parent, root, prefix, processedEdges) ?: return CodeGenResult()
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        javaType.addInitializerBlock(CodeBlock.builder()
                .addStatement("getFields().put(\$S, null)", TypeNameMetaFieldDef.name)
                .build())

        javaType.addMethod(MethodSpec.methodBuilder("toString")
                .returns(ClassName.get(String::class.java))
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addCode("""
                    StringBuilder builder = new StringBuilder();
                    builder.append("... on ${type.name} {");
                    getFields().forEach((k, v) -> {
                        builder.append(" ").append(k);
                        if(v != null) {
                            builder.append(" ").append(v.toString());
                        }
                    });
                    builder.append("}");
            
                    return builder.toString();
                """.trimIndent())
                .build())


        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjection(type: TypeDefinition<*>, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): CodeGenResult {
        val subProjection = createSubProjectionType(type, parent, root, prefix, processedEdges) ?: return CodeGenResult()
        val javaType = subProjection.first
        val codeGenResult = subProjection.second

        val javaFile = JavaFile.builder(getPackageName(), javaType.build()).build()
        return CodeGenResult(clientProjections = listOf(javaFile)).merge(codeGenResult)
    }

    private fun createSubProjectionType(type: TypeDefinition<*>, parent: TypeSpec, root: TypeSpec, prefix: String, processedEdges: Set<Pair<String, String>>): Pair<TypeSpec.Builder, CodeGenResult>? {
        val className = ClassName.get(BaseSubProjectionNode::class.java)
        val clazzName = "${prefix}Projection"
        if(generatedClasses.contains(clazzName)) return null else generatedClasses.add(clazzName)
        val javaType = TypeSpec.classBuilder(clazzName)
                .addModifiers(Modifier.PUBLIC).superclass(ParameterizedTypeName.get(className, ClassName.get(getPackageName(), parent.name), ClassName.get(getPackageName(), root.name)))
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(ClassName.get(getPackageName(), parent.name), "parent").build())
                        .addParameter(ParameterSpec.builder(ClassName.get(getPackageName(), root.name), "root").build())
                        .addCode("super(parent, root);")
                        .build())

            val fieldDefinitions = type.filterInterfaceFields(document) + document.definitions.filterIsInstance<ObjectTypeExtensionDefinition>().filter { it.name == type.name}.flatMap { it.fieldDefinitions }

             val codeGenResult = if(projectionDepth[root.name]?:0 < config.maxProjectionDepth || config.maxProjectionDepth == -1) {
                val depth = projectionDepth.getOrPut(root.name) { 0 }
                projectionDepth[root.name] = depth + 1

                fieldDefinitions.filterSkipped()
                    .mapNotNull { if (it.type.findTypeDefinition(document) != null) Pair(it, it.type.findTypeDefinition(document)) else null }
                    .filter { !processedEdges.contains(Pair(it.second!!.name, type.name)) }
                    .map {
                        val projectionName = "${truncatePrefix(prefix)}${it.first.name.capitalize()}Projection"
                        javaType.addMethod(
                            MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.first.name))
                                .returns(ClassName.get(getPackageName(), projectionName))
                                .addCode(
                                    """
                            $projectionName projection = new $projectionName(this, getRoot());    
                            getFields().put("${it.first.name}", projection);
                            return projection;
                        """.trimIndent()
                                )
                                .addModifiers(Modifier.PUBLIC)
                                .build()
                        )
                        val updatedProcessedEdges = processedEdges.toMutableSet()
                        updatedProcessedEdges.add(Pair(it.second!!.name, type.name))
                        createSubProjection(it.second!!, javaType.build(), root, "${truncatePrefix(prefix)}${it.first.name.capitalize()}", updatedProcessedEdges)
                    }.fold(CodeGenResult()) { total, current -> total.merge(current) }

            } else CodeGenResult()


            fieldDefinitions.filterSkipped()
                .forEach {
                    val objectTypeDefinition = it.type.findTypeDefinition(document)
                    if (objectTypeDefinition == null) {
                        javaType.addMethod(MethodSpec.methodBuilder(ReservedKeywordSanitizer.sanitize(it.name))
                            .returns(ClassName.get(getPackageName(), javaType.build().name))
                            .addCode("""
                        getFields().put("${it.name}", null);
                        return this;
                    """.trimIndent())
                            .addModifiers(Modifier.PUBLIC)
                            .build())
                    }
                }


        val concreteTypesResult = createConcreteTypes(type, root, javaType, prefix, processedEdges)
        val unionTypesResult = createUnionTypes(type, javaType, root, prefix, processedEdges)

        return Pair(javaType, codeGenResult.merge(concreteTypesResult).merge(unionTypesResult))
    }

    private fun truncatePrefix(prefix: String): String {
        return if(config.shortProjectionNames) ClassnameShortener.shorten(prefix) else prefix
    }

    fun getPackageName(): String {
        return config.packageNameClient
    }

    private fun getDatatypesPackageName(): String {
        return config.packageNameTypes
    }
}
