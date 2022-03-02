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

import com.netflix.graphql.dgs.*
import com.netflix.graphql.dgs.codegen.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.TypeSpec.Kind
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

internal class KotlinDataFetcherInterfaceGeneratorTest {

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
        val dataFetchers = codeGenResult.kotlinDataFetchers
        val dataTypes = codeGenResult.kotlinDataTypes

        // THEN
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "QueryDataFetcher"
        )

        val method = dataFetchers[0].members.filterIsInstance<TypeSpec>()[0].funSpecs.first()

        checkInterfaceMethod(method, "people", java.util.List::class, "Person?")
        checkFirstParameterIsDataFetchingEnvironment(method)
        checkAnnotation(method.annotations[0], DgsQuery::class, "field" to "people")

        assertCompilesKotlin(dataFetchers + dataTypes)
    }

    @Test
    fun generateDataFetcherInterfaceForMutation() {
        // GIVEN
        val schema = """
            type Mutation {
                createPerson(person: Person): Boolean!
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        // WHEN
        val codeGenResult = generateCode(schema)
        val dataFetchers = codeGenResult.kotlinDataFetchers
        val dataTypes = codeGenResult.kotlinDataTypes

        // THEN
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "MutationDataFetcher"
        )

        assertThat(dataFetchers[0].members.filterIsInstance<TypeSpec>()).hasSize(1)

        val method = dataFetchers[0].members.filterIsInstance<TypeSpec>()[0].funSpecs.first()
        checkInterfaceMethod(method, "createPerson", Boolean::class)
        checkFirstParameterIsDataFetchingEnvironment(method)

        assertThat(method.parameters).hasSize(2)
        assertThat(method.parameters[1].name).isEqualTo("person")
        assertThat(method.parameters[1].type).isEqualTo("$basePackageName.types.Person?".toKtTypeName())
        assertThat(method.parameters[1].annotations).hasSize(1)
        checkAnnotation(method.parameters[1].annotations[0], InputArgument::class, "name" to "person")

        checkAnnotation(method.annotations[0], DgsMutation::class, "field" to "createPerson")

        assertCompilesKotlin(dataFetchers + dataTypes)
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
        val dataFetchers = codeGenResult.kotlinDataFetchers
        val dataTypes = codeGenResult.kotlinDataTypes

        // THEN
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "SubscriptionDataFetcher"
        )

        assertThat(dataFetchers[0].members.filterIsInstance<TypeSpec>()).hasSize(1)

        val method = dataFetchers[0].members.filterIsInstance<TypeSpec>()[0].funSpecs.first()
        checkInterfaceMethod(method, "onPersonCreated", Publisher::class, "Person?")
        checkAnnotation(method.annotations[0], DgsSubscription::class, "field" to "onPersonCreated")

        assertThat(method.parameters).hasSize(1)
        assertThat(method.parameters[0].name).isEqualTo("dataFetchingEnvironment")
        assertThat(method.parameters[0].type).isEqualTo(DgsDataFetchingEnvironment::class.asTypeName())

        assertCompilesKotlin(dataFetchers + dataTypes)
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
        val dataFetchers = codeGenResult.kotlinDataFetchers
        val dataTypes = codeGenResult.kotlinDataTypes

        // THEN
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataFetchers.size).isEqualTo(1)

        checkDataFetcherInterface(
            javaFile = dataFetchers[0],
            name = "PersonDataFetcher"
        )

        assertThat(dataFetchers[0].members.filterIsInstance<TypeSpec>()).hasSize(1)

        val method = dataFetchers[0].members.filterIsInstance<TypeSpec>()[0].funSpecs.first()
        checkInterfaceMethod(method, "partner", CompletableFuture::class, "Person?")
        checkAnnotation(method.annotations[0], DgsData::class, "field" to "partner", "parentType" to "Person")
        checkFirstParameterIsDataFetchingEnvironment(method)

        assertCompilesKotlin(dataFetchers + dataTypes)
    }

    private fun generateCode(schema: String) = CodeGen(
        CodeGenConfig(
            schemas = setOf(schema),
            packageName = basePackageName,
            language = Language.KOTLIN,
            generateDataFetchersAsInterfaces = true
        )
    ).generate()

    private fun checkInterfaceMethod(method: FunSpec, name: String, clazz: KClass<*>, genericType: String? = null) {
        assertThat(method.name).isEqualTo(name)
        assertThat(method.body.isEmpty()).isTrue
        assertThat(method.modifiers).contains(KModifier.ABSTRACT)
        assertThat(method.annotations).hasSize(1)

        if (genericType != null) {
            assertThat(method.returnType).isInstanceOf(ParameterizedTypeName::class.java)
            assertThat((method.returnType as ParameterizedTypeName).rawType).isEqualTo(clazz.asTypeName())
            assertThat((method.returnType as ParameterizedTypeName).typeArguments).contains(
                "$basePackageName.types.$genericType".toKtTypeName()
            )
        } else {
            assertThat(method.returnType).isEqualTo(clazz.asTypeName())
        }
    }

    private fun checkDataFetcherInterface(javaFile: FileSpec, name: String) {
        assertThat(javaFile.packageName).isEqualTo(dataFetcherPackageName)

        assertThat(javaFile.members.filterIsInstance<TypeSpec>()).hasSize(1)
        val typeSpec = javaFile.members.filterIsInstance<TypeSpec>().first()
        assertThat(typeSpec.name).isEqualTo(name)
        assertThat(typeSpec.funSpecs).hasSize(1)
        assertThat(typeSpec.kind).isEqualTo(Kind.INTERFACE)
    }

    private fun checkFirstParameterIsDataFetchingEnvironment(method: FunSpec) {
        assertThat(method.parameters).hasSizeGreaterThanOrEqualTo(1)
        assertThat(method.parameters[0].name).isEqualTo("dataFetchingEnvironment")
        assertThat(method.parameters[0].type).isEqualTo(DgsDataFetchingEnvironment::class.asTypeName())
    }

    private fun checkAnnotation(annotation: AnnotationSpec, clazz: KClass<*>, vararg pairs: Pair<String, Any>) {
        assertThat(annotation.typeName).isEqualTo(clazz.asTypeName())
        assertThat(annotation.members).hasSize(pairs.size)
        pairs.forEach {
            assertThat(annotation.members).contains(CodeBlock.of("%L = %S", it.first, it.second))
        }
    }

}