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

import com.netflix.graphql.dgs.*
import com.netflix.graphql.dgs.codegen.*
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec.Kind
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

internal class DataFetcherInterfaceGeneratorTest {

    @Test
    fun generateDataFetcherInterfaceForQuery() {
        // GIVEN
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        // WHEN
        val codeGenResult = generateCode(schema)
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        // THEN
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "QueryDataFetcher"
        )

        val method = dataFetchers[0].typeSpec.methodSpecs.first()

        checkInterfaceMethod(method, "people", java.util.List::class, "Person")
        checkFirstParameterIsDataFetchingEnvironment(method)
        checkAnnotation(method.annotations[0], DgsQuery::class, "field" to "people")

        assertCompilesJava(dataFetchers + dataTypes)
    }

    @Test
    fun generateDataFetcherInterfaceForMutation() {
        // GIVEN
        val schema = """
            type Mutation {
                createPerson(person: Person): Boolean
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        // WHEN
        val codeGenResult = generateCode(schema)
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        // THEN
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "MutationDataFetcher"
        )

        val method = dataFetchers[0].typeSpec.methodSpecs.first()
        checkInterfaceMethod(method, "createPerson", java.lang.Boolean::class)
        checkFirstParameterIsDataFetchingEnvironment(method)

        assertThat(method.parameters).hasSize(2)
        assertThat(method.parameters[1].name).isEqualTo("person")
        assertThat(method.parameters[1].type).isEqualTo(ClassName.get("$basePackageName.types", "Person"))
        assertThat(method.parameters[1].annotations).hasSize(1)
        checkAnnotation(method.parameters[1].annotations[0], InputArgument::class, "name" to "person")

        checkAnnotation(method.annotations[0], DgsMutation::class, "field" to "createPerson")

        assertCompilesJava(dataFetchers + dataTypes)
    }

    @Test
    fun generateDataFetcherInterfaceForSubscription() {
        // GIVEN
        val schema = """
            type Subscription {
                onPersonCreated: Person
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        // WHEN
        val codeGenResult = generateCode(schema)
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        // THEN
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "SubscriptionDataFetcher"
        )

        val method = dataFetchers[0].typeSpec.methodSpecs.first()
        checkInterfaceMethod(method, "onPersonCreated", Publisher::class, "Person")
        checkAnnotation(method.annotations[0], DgsSubscription::class, "field" to "onPersonCreated")

        assertThat(method.parameters).hasSize(1)
        assertThat(method.parameters[0].name).isEqualTo("dataFetchingEnvironment")
        assertThat(method.parameters[0].type).isEqualTo(ClassName.get(DgsDataFetchingEnvironment::class.java))

        assertCompilesJava(dataFetchers + dataTypes)
    }

    @Test
    fun generateDataFetcherInterfaceForType() {
        // GIVEN
        val schema = """
            type Person {
                id: ID!
                firstname: String
                lastname: String
                partner: Person
            }
        """.trimIndent()

        // WHEN
        val codeGenResult = generateCode(schema)
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        // THEN
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "PersonDataFetcher"
        )

        val method = dataFetchers[0].typeSpec.methodSpecs.first()
        checkInterfaceMethod(method, "partner", CompletableFuture::class, "Person")
        checkAnnotation(method.annotations[0], DgsData::class, "field" to "partner", "parentType" to "Person")
        checkFirstParameterIsDataFetchingEnvironment(method)

        assertCompilesJava(dataFetchers + dataTypes)
    }

    private fun generateCode(schema: String) = CodeGen(
        CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            generateDataFetchersAsInterfaces = true
        )
    ).generate()

    private fun checkInterfaceMethod(method: MethodSpec, name: String, clazz: KClass<*>, genericType: String? = null) {
        assertThat(method.name).isEqualTo(name)
        assertThat(method.code.isEmpty).isTrue
        assertThat(method.modifiers).contains(Modifier.ABSTRACT)
        assertThat(method.annotations).hasSize(1)

        if (genericType != null) {
            assertThat(method.returnType).isInstanceOf(ParameterizedTypeName::class.java)
            assertThat((method.returnType as ParameterizedTypeName).rawType).isEqualTo(ClassName.get(clazz.java))
            assertThat((method.returnType as ParameterizedTypeName).typeArguments).contains(
                ClassName.get(
                    "$basePackageName.types",
                    genericType
                )
            )
        } else {
            assertThat(method.returnType).isEqualTo(ClassName.get(clazz.java))
        }
    }

    private fun checkDataFetcherInterface(javaFile: JavaFile, name: String) {
        assertThat(javaFile.typeSpec.name).isEqualTo(name)
        assertThat(javaFile.packageName).isEqualTo(dataFetcherPackageName)
        assertThat(javaFile.typeSpec.methodSpecs).hasSize(1)
        assertThat(javaFile.typeSpec.kind).isEqualTo(Kind.INTERFACE)
    }

    private fun checkFirstParameterIsDataFetchingEnvironment(method: MethodSpec) {
        assertThat(method.parameters).hasSizeGreaterThanOrEqualTo(1)
        assertThat(method.parameters[0].name).isEqualTo("dataFetchingEnvironment")
        assertThat(method.parameters[0].type).isEqualTo(ClassName.get(DgsDataFetchingEnvironment::class.java))
    }

    private fun checkAnnotation(annotation: AnnotationSpec, clazz: KClass<*>, vararg members: Pair<String, Any>) {
        assertThat(annotation.type).isEqualTo(ClassName.get(clazz.java))
        assertThat(annotation.members).hasSize(members.size)
        members.forEach {
            assertThat(annotation.members).containsKey(it.first)
            assertThat(annotation.members[it.first]).hasSize(1)
            assertThat(annotation.members[it.first]?.first().toString()).isEqualTo("\"${it.second}\"")
        }
    }

}