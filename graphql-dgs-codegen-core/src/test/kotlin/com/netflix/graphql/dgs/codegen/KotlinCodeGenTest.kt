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

class KotlinCodeGenTest {

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        val (countProperty, truthProperty, floatyProperty) = type.propertySpecs
        assertThat(countProperty.type).isEqualTo(Int::class.asTypeName().copy(nullable = true))
        assertThat(truthProperty.type).isEqualTo(Boolean::class.asTypeName().copy(nullable = true))
        assertThat(floatyProperty.type).isEqualTo(Double::class.asTypeName().copy(nullable = true))

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.mypackage",
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.name).isEqualTo("name")
        assertThat(emailProperty.name).isEqualTo("email")

        assertThat(nameProperty.type).isEqualTo(STRING.copy(nullable = true))
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING.copy(nullable = true)).copy(nullable = true))
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.type).isEqualTo(STRING)
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING))
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs

        assertThat(nameProperty.type).isEqualTo(STRING)
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING.copy(nullable = true)))
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

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        Truth.assertThat(FileSpec.get("$basePackageName.types", type).toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonProperty
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |import kotlin.String
                |
                |@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
                |public data class Employee(
                |  @JsonProperty("firstname")
                |  public override val firstname: String? = null,
                |  @JsonProperty("lastname")
                |  public override val lastname: String? = null,
                |  @JsonProperty("company")
                |  public val company: String? = null
                |) : Person
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

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "friends")

        val (firstName, lastName, friends) = type.propertySpecs

        assertThat(firstName.type).isEqualTo(STRING.copy(nullable = true))
        assertThat(lastName.type).isEqualTo(STRING.copy(nullable = true))
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

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

        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(Pair("Date", "java.time.LocalDateTime"))
        )).generate() as KotlinCodeGenResult
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


        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(type.propertySpecs.size).isEqualTo(1)
        assertThat(type.propertySpecs).extracting("name").contains("genre")
    }

    @Test
    fun generateInputWithDefaultValueForEnum() {
        val schema = """
            enum Color {
                red
            }
            
            input ColorFilter {
                color: Color = red
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("ColorFilter")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("color")
        assertThat(colorParam.type.toString()).isEqualTo("$typesPackageName.Color?")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("$typesPackageName.Color.red")
    }

    @Test
    fun generateInputWithEmptyDefaultValueForArray() {
        val schema = """
            input SomeType {
                names: [String!]! = []
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("names")
        assertThat(colorParam.type.toString()).isEqualTo("kotlin.collections.List<kotlin.String>")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("emptyList()")
    }

    @Test
    fun generateInputWithDefaultValueForStringArray() {
        val schema = """
            input SomeType {
                names: [String!]! = ["DGS"]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("names")
        assertThat(colorParam.type.toString()).isEqualTo("kotlin.collections.List<kotlin.String>")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("""listOf("DGS")""")
    }

    @Test
    fun generateInputWithDefaultValueForNullableStringArray() {
        val schema = """
            input SomeType {
                names: [String]! = ["DGS"]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("names")
        assertThat(colorParam.type.toString()).isEqualTo("kotlin.collections.List<kotlin.String?>")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("""listOf("DGS")""")
    }

    @Test
    fun generateInputWithDefaultValueForIntArray() {
        val schema = """
            input SomeType {
                names: [Int!]! = [1,2,3]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("names")
        assertThat(colorParam.type.toString()).isEqualTo("kotlin.collections.List<kotlin.Int>")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("""listOf(1, 2, 3)""")
    }

    @Test
    fun generateInputWithDefaultValueForEnumArray() {
        val schema = """
            input SomeType {
                colors: [Color!]! = [red]
            }
            
            enum Color {
                red,
                blue
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("colors")
        assertThat(colorParam.type.toString()).isEqualTo("kotlin.collections.List<com.netflix.graphql.dgs.codegen.tests.generated.types.Color>")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("""listOf(com.netflix.graphql.dgs.codegen.tests.generated.types.Color.red)""")
    }

    @Test
    fun generateInputWithDefaultValueForBooleanArray() {
        val schema = """
            input SomeType {
                booleans: [Bool!]! = [true]
            }          
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val members = data.members
        assertThat(members).hasSize(1)

        val type = members[0] as TypeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val ctorSpec = type.primaryConstructor
        assertThat(ctorSpec).isNotNull
        assertThat(ctorSpec!!.parameters).hasSize(1)

        val colorParam = ctorSpec.parameters[0]
        assertThat(colorParam.name).isEqualTo("booleans")
        assertThat(colorParam.defaultValue).isNotNull
        assertThat(colorParam.defaultValue.toString()).isEqualTo("""listOf(true)""")
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + "${'$'}{if(genre != null) "\"" else ""}" + genre + "${'$'}{if(genre != null) "\"" else ""}" + "," +"rating:" + rating + "," +"views:" + views + "," +"stars:" + stars + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToStringMethodForNonNullableInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: String!
                rating: Int!
                views: Int
                stars: Int
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + "\"" + genre + "\"" + "," +"rating:" + rating + "," +"views:" + views + "," +"stars:" + stars + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToInputStringMethodForNonNullableInputTypesWithDefaults() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: String! = "horror"
                rating: Int! = 3
                average: Float! = 1.2
                viewed: Boolean! = true
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + "\"" + genre + "\"" + "," +"rating:" + rating + "," +"average:" + average + "," +"viewed:" + viewed + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
    }

    @Test
    fun generateToInputStringMethodForInputTypesWithDefaults() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            enum Colors {
                blue
                red
                yellow
            }
            input MovieFilter {
                genre: String = "horror"
                rating: Int = 3
                average: Float = 1.2
                viewed: Boolean = true
                identifier: ID = "jhw"
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + "${'$'}{if(genre != null) "\"" else ""}" + genre + "${'$'}{if(genre != null) "\"" else ""}" + "," +"rating:" + rating + "," +"average:" + average + "," +"viewed:" + viewed + "," +"identifier:" + "${'$'}{if(identifier != null) "\"" else ""}" + identifier + "${'$'}{if(identifier != null) "\"" else ""}" + "" +"}"
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
                actors: [String]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        var expectedInputString = """
            return "{" + "genre:" + serializeListOfString(genre) + "," +"actors:" + serializeListOfString(actors) + "" +"}"
        """.trimIndent()
        var generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)


        assertThat(type.funSpecs).extracting("name").contains("serializeListOfString")
        expectedInputString = """
            if (inputList == null) {
                    return null
                }
                val builder = java.lang.StringBuilder()
                builder.append("[")
                if (! inputList.isEmpty()) {
                    val result = inputList.joinToString() {"\"" + it + "\""}
                    builder.append(result)
                }
                builder.append("]")
                return  builder.toString()
        """.trimIndent()
        generatedInputString = type.funSpecs.single { it.name == "serializeListOfString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + genre + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
        )).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
    }

    @Test
    fun generateToInputStringMethodForDateTypes() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                localDateTime: LocalDateTime
                localDate: LocalDate
                localTime: LocalTime
                dateTime: DateTime
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
           return "{" + "localDateTime:" + "${'$'}{if(localDateTime != null) "\"" else ""}" + localDateTime + "${'$'}{if(localDateTime != null) "\"" else ""}" + "," +"localDate:" + "${'$'}{if(localDate != null) "\"" else ""}" + localDate + "${'$'}{if(localDate != null) "\"" else ""}" + "," +"localTime:" + "${'$'}{if(localTime != null) "\"" else ""}" + localTime + "${'$'}{if(localTime != null) "\"" else ""}" + "," +"dateTime:" + "${'$'}{if(dateTime != null) "\"" else ""}" + dateTime + "${'$'}{if(dateTime != null) "\"" else ""}" + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
    }

    @Test
    fun generateToInputStringMethodForListOfDate() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                names: [String]
                localDateTime: [LocalDateTime]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "names:" + serializeListOfString(names) + "," +"localDateTime:" + serializeListOfLocalDateTime(localDateTime) + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
    }

    @Test
    fun generateToInputStringMethodForTypeMappedTypes() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                time: LocalTime
                date: LocalDate
                
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN,
                typeMapping = mapOf(Pair("LocalTime", "java.time.LocalDateTime"), Pair("LocalDate", "java.lang.Integer"))
        )).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "time:" + "${'$'}{if(time != null) "\"" else ""}" + time + "${'$'}{if(time != null) "\"" else ""}" + "," +"date:" + date + "" +"}"
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
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


        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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


        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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


        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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


        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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


        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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


        val result = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
        )).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Person")
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.propertySpecs).extracting("name").containsExactly("name")
    }

    @Test
    fun generateWithCustomSubPackageName() {

        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, subPackageNameTypes = "mytypes", language = Language.KOTLIN)).generate() as KotlinCodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("$basePackageName.mytypes")
    }

    @Test
    fun generateDataClassWithInterfaceInheritance() {

        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String
                lastname: String
            }

            interface Employee implements Person {
                firstname: String
                lastname: String
                company: String
            }

            type Talent implements Employee {
                firstname: String
                lastname: String
                company: String
                imdbProfile: String
            }

        """.trimIndent()

        val (dataTypes, interfaces) =
                CodeGen(CodeGenConfig(
                        schemas = setOf(schema),
                        packageName = basePackageName,
                        language = Language.KOTLIN)
                ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Talent")
        assertThat(type.propertySpecs.size).isEqualTo(4)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company", "imdbProfile")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.superinterfaces.keys)
                .contains(ClassName
                        .bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.Employee"))


        //Check interface
        assertThat(interfaces.size).isEqualTo(2)

        val personInterfaceType = interfaces[0].members[0] as TypeSpec
        Truth.assertThat(FileSpec.get("$basePackageName.types", personInterfaceType).toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import kotlin.String
                |
                |public interface Person {
                |  public val firstname: String?
                |
                |  public val lastname: String?
                |}
                |""".trimMargin())

        val employeeInterfaceType = interfaces[1].members[0] as TypeSpec
        Truth.assertThat(FileSpec.get("$basePackageName.types", employeeInterfaceType).toString()).isEqualTo(
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
                |  JsonSubTypes.Type(value = Talent::class, name = "Talent")
                |])
                |public interface Employee {
                |  public val firstname: String?
                |
                |  public val lastname: String?
                |
                |  public val company: String?
                |}
                |""".trimMargin())

        Truth.assertThat(FileSpec.get("$basePackageName.types", type).toString()).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonProperty
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |import kotlin.String
                |
                |@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
                |public data class Talent(
                |  @JsonProperty("firstname")
                |  public override val firstname: String? = null,
                |  @JsonProperty("lastname")
                |  public override val lastname: String? = null,
                |  @JsonProperty("company")
                |  public override val company: String? = null,
                |  @JsonProperty("imdbProfile")
                |  public val imdbProfile: String? = null
                |) : Employee
                |""".trimMargin())
    }


    @Test
    fun generateWithJavaTypeDirective() {
        val schema = """
          type Query {
              movieCountry(movieId: MovieID): MovieCountry
          }
          
          type MovieCountry {
            country: String
            movieId: MovieID
          }
          scalar MovieID @javaType(name : "com.example.MyType")
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf(), language = Language.KOTLIN)).generate() as KotlinCodeGenResult
        assertThat(codeGenResult.dataTypes.first().toString()).contains("com.example.MyType")
        assertThat(codeGenResult.queryTypes.first().toString()).contains("com.example.MyType")
    }
}
