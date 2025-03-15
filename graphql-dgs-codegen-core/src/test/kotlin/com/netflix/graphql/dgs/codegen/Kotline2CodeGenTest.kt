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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import graphql.schema.DataFetchingEnvironment
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.Serializable

class Kotline2CodeGenTest {
    @Test
    fun generateSerializableDataClass() {
        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateKotlinClosureProjections = true,
                implementSerializable = true
            )
        ).generate()

        val dataTypes = codeGenResult.kotlinDataTypes

        assertThat(dataTypes).hasSize(1)
        assertThat(dataTypes.first().packageName).isEqualTo(typesPackageName)
        assertThat(dataTypes.first().members).singleElement()
            .satisfies({ member ->
                val typeSpec = member as TypeSpec
                assertThat(typeSpec.superinterfaces).containsKey(Serializable::class.asClassName())
            })
    }

    @Test
    fun `adds @Deprecated annotation from schema directives when setting enabled`() {
        val schema = """
            enum TownJobTypes {
                LAMPLIGHTER @deprecated(reason: "town switched to electric lights")
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                addDeprecatedAnnotation = true,
                generateKotlinNullableClasses = true
            )
        ).generate()
        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$basePackageName.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |import kotlin.Deprecated
                |
                |public enum class TownJobTypes {
                |  @Deprecated(message = "town switched to electric lights")
                |  LAMPLIGHTER,
                |  ;
                |
                |  public companion object
                |}
                |
            """.trimMargin()

        )
        assertCompilesKotlin(result.kotlinEnumTypes)
    }

    @Test
    fun `Add companion object to enum class`() {
        val schema = """
            enum MyEnum {
                A
                B
                C
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()

        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        assertThat(FileSpec.get("$basePackageName.enums", type).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.enums
                |
                |public enum class MyEnum {
                |  A,
                |  B,
                |  C,
                |  ;
                |
                |  public companion object
                |}
                |
            """.trimMargin()
        )

        assertCompilesKotlin(result.kotlinEnumTypes)
    }

    @Test
    fun generateDataFetcherInterfaceWithFunction() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val dataFetchers = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateDataFetcherInterfaces = true
            )
        ).generate().kotlinDataFetchers

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].name).isEqualTo("PeopleQuery")
        assertThat(dataFetchers[0].packageName).isEqualTo("$basePackageName.datafetchers")
        val type = dataFetchers[0].members[0] as TypeSpec

        assertThat(type.kind).isEqualTo(TypeSpec.Kind.INTERFACE)
        assertThat(type.annotations).hasSize(1).first().satisfies({
            assertThat(it.typeName.toString()).isEqualTo("com.netflix.graphql.dgs.DgsComponent")
        })
        assertThat(type.funSpecs).hasSize(1)
        val fn = type.funSpecs.single()
        assertThat(fn.name).isEqualTo("people")
        val returnType = fn.returnType as ParameterizedTypeName
        assertThat(fn.returnType)
        assertThat(returnType.rawType.canonicalName).isEqualTo(List::class.qualifiedName)
        assertThat(returnType.typeArguments).hasSize(1)
        val arg0 = returnType.typeArguments.single() as ClassName
        assertThat(arg0.canonicalName).isEqualTo("$typesPackageName.Person")
        assertThat(fn.parameters).hasSize(1)
        val param0 = fn.parameters.single()
        assertThat(param0.name).isEqualTo("dataFetchingEnvironment")
        assertThat((param0.type as ClassName).canonicalName).isEqualTo(DataFetchingEnvironment::class.qualifiedName)
        assertThat(fn.annotations).hasSize(1).first().satisfies({ annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("com.netflix.graphql.dgs.DgsData")
            assertThat(annotation.members).satisfiesExactly(
                { member -> assertThat(member.toString()).isEqualTo("parentType = DgsConstants.QUERY.TYPE_NAME") },
                { member -> assertThat(member.toString()).isEqualTo("field = DgsConstants.QUERY.People") }
            )
        })
    }

    @Test
    fun generateDataFetcherInterfaceWithArgument() {
        val schema = """
            type Query {
                person(name: String): Person
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val dataFetchers = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateDataFetcherInterfaces = true
            )
        ).generate().kotlinDataFetchers

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].name).isEqualTo("PersonQuery")
        assertThat(dataFetchers[0].packageName).isEqualTo("$basePackageName.datafetchers")
        val type = dataFetchers[0].members[0] as TypeSpec

        assertThat(type.kind).isEqualTo(TypeSpec.Kind.INTERFACE)
        assertThat(type.funSpecs).hasSize(1)
        val fn = type.funSpecs.single()
        assertThat(fn.name).isEqualTo("person")
        assertThat((fn.returnType as ClassName).canonicalName).isEqualTo("$typesPackageName.Person")
        assertThat(fn.parameters).hasSize(2)
        val arg0 = fn.parameters[0]
        assertThat(arg0.name).isEqualTo("name")
        assertThat((arg0.type as ClassName).canonicalName).isEqualTo(String::class.qualifiedName)
        assertThat(arg0.annotations).hasSize(1)
        val arg0Annotation = arg0.annotations[0]
        assertThat(arg0Annotation.typeName.toString()).isEqualTo("com.netflix.graphql.dgs.InputArgument")
        assertThat(arg0Annotation.members.single().toString()).isEqualTo("DgsConstants.QUERY.PERSON_INPUT_ARGUMENT.Name")
        val arg1 = fn.parameters[1]
        assertThat(arg1.name).isEqualTo("dataFetchingEnvironment")
        assertThat((arg1.type as ClassName).canonicalName).isEqualTo(DataFetchingEnvironment::class.qualifiedName)
    }

    @Test
    fun generateMutationInterfaceWithArgument() {
        val schema = """
            type Mutation {
                addPerson(person: Person): Person
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val dataFetchers = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateDataFetcherInterfaces = true
            )
        ).generate().kotlinDataFetchers

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].name).isEqualTo("AddPersonMutation")
        assertThat(dataFetchers[0].packageName).isEqualTo("$basePackageName.datafetchers")
        val type = dataFetchers[0].members[0] as TypeSpec

        assertThat(type.kind).isEqualTo(TypeSpec.Kind.INTERFACE)
        assertThat(type.funSpecs).hasSize(1)
        val fn = type.funSpecs.single()
        assertThat(fn.name).isEqualTo("addPerson")
        assertThat((fn.returnType as ClassName).canonicalName).isEqualTo("$typesPackageName.Person")
        assertThat(fn.parameters).hasSize(2)
        val arg0 = fn.parameters[0]
        assertThat(arg0.name).isEqualTo("person")
        assertThat((arg0.type as ClassName).canonicalName).isEqualTo("$typesPackageName.Person")
        assertThat(arg0.annotations).hasSize(1)
        val arg0Annotation = arg0.annotations[0]
        assertThat(arg0Annotation.typeName.toString()).isEqualTo("com.netflix.graphql.dgs.InputArgument")
        assertThat(arg0Annotation.members.single().toString()).isEqualTo("DgsConstants.MUTATION.ADDPERSON_INPUT_ARGUMENT.Person")
        val arg1 = fn.parameters[1]
        assertThat(arg1.name).isEqualTo("dataFetchingEnvironment")
        assertThat((arg1.type as ClassName).canonicalName).isEqualTo(DataFetchingEnvironment::class.qualifiedName)
    }

    @Test
    fun generateSubscriptionInterfaceWithArgument() {
        val schema = """
            type Subscription {
                personUpdated(id: Int): Person
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val dataFetchers = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateKotlinNullableClasses = true,
                generateDataFetcherInterfaces = true
            )
        ).generate().kotlinDataFetchers

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].name).isEqualTo("PersonUpdatedSubscription")
        assertThat(dataFetchers[0].packageName).isEqualTo("$basePackageName.datafetchers")
        val type = dataFetchers[0].members[0] as TypeSpec

        assertThat(type.kind).isEqualTo(TypeSpec.Kind.INTERFACE)
        assertThat(type.funSpecs).hasSize(1)
        val fn = type.funSpecs.single()
        assertThat(fn.name).isEqualTo("personUpdated")
        assertThat((fn.returnType as ParameterizedTypeName).rawType.canonicalName).isEqualTo("org.reactivestreams.Publisher")
        assertThat((fn.returnType as ParameterizedTypeName).typeArguments[0].toString()).isEqualTo("$typesPackageName.Person?")
        assertThat(fn.parameters).hasSize(2)
        val arg0 = fn.parameters[0]
        assertThat(arg0.name).isEqualTo("id")
        assertThat((arg0.type as ClassName).canonicalName).isEqualTo("kotlin.Int")
        assertThat(arg0.annotations).hasSize(1)
        val arg0Annotation = arg0.annotations[0]
        assertThat(arg0Annotation.typeName.toString()).isEqualTo("com.netflix.graphql.dgs.InputArgument")
        assertThat(arg0Annotation.members.single().toString()).isEqualTo("DgsConstants.SUBSCRIPTION.PERSONUPDATED_INPUT_ARGUMENT.Id")
        val arg1 = fn.parameters[1]
        assertThat(arg1.name).isEqualTo("dataFetchingEnvironment")
        assertThat((arg1.type as ClassName).canonicalName).isEqualTo(DataFetchingEnvironment::class.qualifiedName)
    }
}
