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

import com.google.common.truth.Truth
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


@ExperimentalStdlibApi
internal class KotlinCodeGenTest {

    val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"
    val typesPackageName = "$basePackageName.types"

    @Test
    fun generateDataClassWithStringProperties() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.modifiers).contains(KModifier.DATA)
        assertThat(type.propertySpecs.size).isEqualTo(2)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname")
    }

    @Test
    fun generateDataClassWithNullablePrimitive() {

        val schema = """
            type MyType {
                count: Int
                truth: Boolean
                floaty: Float
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        val (countProperty, truthProperty, floatyProperty) = type.propertySpecs
        assertThat(countProperty.type).isEqualTo(typeNameOf<Int?>())
        assertThat(truthProperty.type).isEqualTo(typeNameOf<Boolean?>())
        assertThat(floatyProperty.type).isEqualTo(typeNameOf<Double?>())

        val constructor = type.primaryConstructor
                ?: throw AssertionError("No primary constructor found")
        assertThat(constructor.parameters.size).isEqualTo(3)
        for (param in constructor.parameters) {
            assertThat(param.defaultValue.toString()).isEqualTo("null")
        }
    }

    @Test
    fun generateDataClassWithNonNullablePrimitive() {

        val schema = """
            type MyType {
                count: Int!
                truth: Boolean!
                floaty: Float!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("kotlin.Int")
        assertThat(type.propertySpecs[1].type.toString()).isEqualTo("kotlin.Boolean")
        assertThat(type.propertySpecs[2].type.toString()).isEqualTo("kotlin.Double")

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNull()
        assertThat(type.primaryConstructor!!.parameters[1].defaultValue).isNull()
        assertThat(type.primaryConstructor!!.parameters[2].defaultValue).isNull()
    }

    @Test
    fun generateDataClassWithNonNullableComplexType() {

        val schema = """
            type MyType {
                other: OtherType!
            }
            
            type OtherType {
                name: String!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.types.OtherType")
        assertThat(type.propertySpecs[0].type.isNullable).isFalse()

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNull()
    }

    @Test
    fun generateDataClassWithNoFields() {

        val schema = """
            type Query {
                me: Person
            }

            type Person {
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName,  language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes.size).isEqualTo(1)
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.name).isEqualTo("Person")
    }

    @Test
    fun generateDataClassWithCustomPackagename() {

        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = "com.mypackage",  language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("com.mypackage.types")
    }

    @Test
    fun generateDataClassWithListProperties() {

        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                name: String
                email: [String]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.name).isEqualTo("name")
        assertThat(emailProperty.name).isEqualTo("email")

        assertThat(nameProperty.type).isEqualTo(typeNameOf<String?>())
        assertThat(emailProperty.type).isEqualTo(typeNameOf<List<String?>?>())
    }

    @Test
    fun generateDataClassWithNonNullableProperties() {
        val schema = """
            type Query {
                people: [Person!]
            }

            type Person {
                name: String!
                email: [String!]!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.type).isEqualTo(typeNameOf<String>())
        assertThat(emailProperty.type).isEqualTo(typeNameOf<List<String>>())
    }

    @Test
    fun generateDataClassWithNonNullableListOfNullableValues() {
        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                name: String!
                email: [String]!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs

        assertThat(nameProperty.type).isEqualTo(typeNameOf<String>())
        assertThat(emailProperty.type).isEqualTo(typeNameOf<List<String?>>())
    }

    @Test
    fun generateDataClassWithInterface() {

        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String
                lastname: String
            }

            type Employee implements Person {
                firstname: String
                lastname: String
                company: String
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Employee")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.Person"))

        //Check interface
        assertThat(interfaces.size).isEqualTo(1)
        val interfaceType = interfaces[0].members[0] as TypeSpec

        Truth.assertThat(FileSpec.get("$basePackageName.types", interfaceType).toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonSubTypes
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |import kotlin.String
                |
                |@JsonTypeInfo(
                |  use = JsonTypeInfo.Id.NAME,
                |  include = JsonTypeInfo.As.PROPERTY,
                |  property = "__typename"
                |)
                |@JsonSubTypes(value = [
                |  JsonSubTypes.Type(value = Employee::class, name = "Employee")
                |])
                |public interface Person {
                |  public val firstname: String?
                |
                |  public val lastname: String?
                |}
                |""".trimMargin())
    }

    @Test
    fun generateDataClassWithNonNullableAndInterface() {

        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String!
                lastname: String!
                company: String
            }

            type Employee implements Person {
                firstname: String!
                lastname: String!
                company: String
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Employee")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.primaryConstructor?.parameters?.get(0)?.type?.isNullable).isFalse()
        assertThat(type.primaryConstructor?.parameters?.get(1)?.type?.isNullable).isFalse()
        assertThat(type.primaryConstructor?.parameters?.get(2)?.type?.isNullable).isTrue()

        //Check interface
        assertThat(interfaces.size).isEqualTo(1)
        val interfaceType = interfaces[0].members[0] as TypeSpec
        assertThat(interfaceType.name).isEqualTo("Person")
        assertThat(interfaceType.propertySpecs.size).isEqualTo(3)
        assertThat(interfaceType.propertySpecs[0].type.isNullable).isEqualTo(false)
        assertThat(interfaceType.propertySpecs[1].type.isNullable).isEqualTo(false)
        assertThat(interfaceType.propertySpecs[2].type.isNullable).isEqualTo(true)
    }

    @Test
    fun generateDataClassWitRecursiveField() {

        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                firstname: String
                lastname: String
                friends: [Person]
            }

        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "friends")

        val (firstName, lastName, friends) = type.propertySpecs

        assertThat(firstName.type).isEqualTo(typeNameOf<String?>())
        assertThat(lastName.type).isEqualTo(typeNameOf<String?>())
        val personClass = ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.Person")
        val friendsType = LIST.parameterizedBy(personClass.copy(nullable = true)).copy(nullable = true)
        assertThat(friends.type).isEqualTo(friendsType)
    }

    @Test
    fun generateDataClassWitDeeplyNestedComplexField() {
        val schema = """
            type Query {
                cars: [Car]
            }

            type Car {
                make: String
                model: String
                engine: Engine
            }

            type Engine {
                type: String
                bhp: Int
                size: Float
                performance: Performance
            }

            type Performance {
                zeroToSixty: Float
                quarterMile: Float
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(dataTypes).flatExtracting("members").extracting("name").contains("Car", "Engine", "Performance")
        val nestedType = dataTypes[1].members[0] as TypeSpec
        assertThat(nestedType.name).isEqualTo("Engine")
        assertThat(nestedType.propertySpecs).filteredOn("name", "performance").extracting("type.simpleName").containsExactly("Performance")

    }

    @Test
    fun generateEnum() {

        val schema = """
            type Query {
                people: [Person]
            }

            enum EmployeeTypes {
                ENGINEER
                MANAGER
                DIRECTOR
            }
        """.trimIndent()

        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.enumTypes[0].members[0] as TypeSpec

        //Check generated enum type
        assertThat(type.name).isEqualTo("EmployeeTypes")
        assertThat(type.enumConstants.size).isEqualTo(3)
        assertThat(type.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")
    }

    @Test
    fun generateDataClassesWithMappedTypes() {

        val schema = """
            type Query {
                now: Date
                person: Person
            }

            type Person {
                firstname: String
                lastname: String
                birthDate: Date
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, typeMapping = mapOf(Pair("Date", "java.time.LocalDateTime")), language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "birthDate")
        assertThat(type.propertySpecs[2].type.toString()).isEqualTo("java.time.LocalDateTime?")
    }

    @Test
    fun generateInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }

            input MovieFilter {
                genre: String
            }
        """.trimIndent()


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(type.propertySpecs.size).isEqualTo(1)
        assertThat(type.propertySpecs).extracting("name").contains("genre")
    }

    @Test
    fun generateToStringMethodForInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: String
                rating: Int
                views: Int
                stars: Int
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:\"" + genre + "\"," +"rating:" + rating + "," +"views:" + views + "," +"stars:" + stars + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToStringMethodForListOfStrings() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: [String!]
                actors: [String!]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        var expectedInputString = """
            return "{" + "genre:" + serializeListOfStrings(genre) + "," +"actors:" + serializeListOfStrings(actors) + "" +"}"
        """.trimIndent()
        var generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)


        assertThat(type.funSpecs).extracting("name").contains("serializeListOfStrings")
        expectedInputString = """
                val builder = java.lang.StringBuilder()
                builder.append("[")
            
                val result = genre?.joinToString() {"\"" + it + "\""}
                builder.append(result)
                builder.append("]")
                return  builder.toString()
        """.trimIndent()
        generatedInputString = type.funSpecs.single { it.name == "serializeListOfStrings" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToStringMethodForListOfIntegers() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: [Integer]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        var expectedInputString = """
            return "{" + "genre:" + genre + "" +"}"
        """.trimIndent()
        var generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToStringMethodForDataTypes() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
    }

    @Test
    fun generateExtendedInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }

            input MovieFilter {
                genre: String
            }

            extend input MovieFilter {
                releaseYear: Int
            }
        """.trimIndent()


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")

        assertThat(type.propertySpecs.size).isEqualTo(2)
        assertThat(type.propertySpecs).extracting("name").contains("genre", "releaseYear")
    }

    @Test
    fun generateConstants() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "People")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname")
    }

    @Test
    fun generateConstantsForInputTypes() {
        val schema = """
            type Query {
                people(filter: PersonFilter): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            input PersonFilter {
                email: String
            }
        """.trimIndent()


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email")
    }

    @Test
    fun generateConstantsWithExtendedInputTypes() {
        val schema = """
            type Query {
                people(filter: PersonFilter): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            input PersonFilter {
                email: String
            }
            
            extend input PersonFilter {
                birthYear: Int
            }
        """.trimIndent()


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email", "BirthYear")
    }

    @Test
    fun generateConstantsWithExtendedTypes() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            extend type Person {
                email: String
            }
        """.trimIndent()


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Email")
    }

    @Test
    fun generateConstantsWithExtendedQuery() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            extend type Query {
                friends: [Person]
            }
        """.trimIndent()


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "People", "Friends")
    }

    @Test
    fun generateUnion() {
        val schema = """
            type Query {
                search: [SearchResult]
            }
            
            union SearchResult = Movie | Actor

            type Movie {
                title: String
            }

            type Actor {
                name: String
            }
            
            
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Movie", "Actor")
        assertThat(interfaces).extracting("name").containsExactly("SearchResult")
        val typeSpec = dataTypes[0].members[0] as TypeSpec

        assertThat(typeSpec.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.SearchResult"))
    }

    @Test
    fun skipCodegenOnTypes() {
        val schema = """
            type Person {
                name: String
            }

            type Car @skipcodegen {
                make: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Person")
    }

    @Test
    fun skipCodegenOnFields() {
        val schema = """
            type Person {
                name: String
                email: String @skipcodegen
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Person")
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.propertySpecs).extracting("name").containsExactly("name")
    }
}