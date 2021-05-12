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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URLClassLoader
import java.util.stream.Stream

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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.modifiers).contains(KModifier.DATA)
        assertThat(type.propertySpecs.size).isEqualTo(2)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname")

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

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

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("kotlin.Int")
        assertThat(type.propertySpecs[1].type.toString()).isEqualTo("kotlin.Boolean")
        assertThat(type.propertySpecs[2].type.toString()).isEqualTo("kotlin.Double")

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNull()
        assertThat(type.primaryConstructor!!.parameters[1].defaultValue).isNull()
        assertThat(type.primaryConstructor!!.parameters[2].defaultValue).isNull()

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun `non nullable primitive, but with kotlinAllFieldsOptional setting`() {

        val schema = """
            type MyType {
                count: Int!
                truth: Boolean!
                floaty: Float!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                kotlinAllFieldsOptional = true
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        val (countProperty, truthProperty, floatyProperty) = type.propertySpecs
        assertThat(countProperty.type).isEqualTo(Int::class.asTypeName().copy(nullable = true))
        assertThat(truthProperty.type).isEqualTo(Boolean::class.asTypeName().copy(nullable = true))
        assertThat(floatyProperty.type).isEqualTo(Double::class.asTypeName().copy(nullable = true))

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.types.OtherType")
        assertThat(type.propertySpecs[0].type.isNullable).isFalse

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNull()

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun `non nullable complex type with kotlinAllFieldsOptional setting`() {

        val schema = """
            type MyType {
                other: OtherType!
            }
            
            type OtherType {
                name: String!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                kotlinAllFieldsOptional = true,
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.types.OtherType?")
        assertThat(type.propertySpecs[0].type.isNullable).isTrue

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNotNull

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        assertThat(dataTypes.size).isEqualTo(1)
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.name).isEqualTo("Person")
        val companion = type.typeSpecs[0]
        assertThat(companion.isCompanion).isTrue
        assertThat(companion.name).isNull()

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.mypackage",
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("com.mypackage.types")

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.name).isEqualTo("name")
        assertThat(emailProperty.name).isEqualTo("email")

        assertThat(nameProperty.type).isEqualTo(STRING.copy(nullable = true))
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING.copy(nullable = true)).copy(nullable = true))

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs
        assertThat(nameProperty.type).isEqualTo(STRING)
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING))

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(type.propertySpecs.size).isEqualTo(2)

        val (nameProperty, emailProperty) = type.propertySpecs

        assertThat(nameProperty.type).isEqualTo(STRING)
        assertThat(emailProperty.type).isEqualTo(LIST.parameterizedBy(STRING.copy(nullable = true)))

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        // Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Employee")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.Person"))

        // Check interface
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
                |""".trimMargin()
        )

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
                |) : Person {
                |  public companion object
                |}
                |""".trimMargin()
        )

        assertCompilesKotlin(dataTypes + interfaces)
    }

    @Test
    fun generateDataClassWithExtendedInterface() {
        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String!
                lastname: String
            }

            extend interface Person {
                age: Int
            }
        """.trimIndent()

        val (_, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        assertThat(interfaces.size).isEqualTo(1)

        val interfaceType = interfaces[0].members[0] as TypeSpec
        assertThat(interfaceType.propertySpecs.size).isEqualTo(3)
        assertThat(interfaceType.propertySpecs).extracting("name").containsExactly("firstname", "lastname", "age")

        assertCompilesKotlin(interfaces)
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

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        // Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Employee")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.primaryConstructor?.parameters?.get(0)?.type?.isNullable).isFalse()
        assertThat(type.primaryConstructor?.parameters?.get(1)?.type?.isNullable).isFalse()
        assertThat(type.primaryConstructor?.parameters?.get(2)?.type?.isNullable).isTrue()

        // Check interface
        assertThat(interfaces.size).isEqualTo(1)
        val interfaceType = interfaces[0].members[0] as TypeSpec
        assertThat(interfaceType.name).isEqualTo("Person")
        assertThat(interfaceType.propertySpecs.size).isEqualTo(3)
        assertThat(interfaceType.propertySpecs[0].type.isNullable).isEqualTo(false)
        assertThat(interfaceType.propertySpecs[1].type.isNullable).isEqualTo(false)
        assertThat(interfaceType.propertySpecs[2].type.isNullable).isEqualTo(true)

        assertCompilesKotlin(dataTypes + interfaces)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        // Check data class
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

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        assertThat(dataTypes).flatExtracting("members").extracting("name").contains("Car", "Engine", "Performance")
        val nestedType = dataTypes[1].members[0] as TypeSpec
        assertThat(nestedType.name).isEqualTo("Engine")
        assertThat(nestedType.propertySpecs).filteredOn("name", "performance").extracting("type.simpleName").containsExactly("Performance")

        assertCompilesKotlin(dataTypes)
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = result.enumTypes[0].members[0] as TypeSpec

        // Check generated enum type
        assertThat(type.name).isEqualTo("EmployeeTypes")
        assertThat(type.enumConstants.size).isEqualTo(3)
        assertThat(type.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")

        assertCompilesKotlin(result.dataTypes + result.enumTypes)
    }

    @Test
    fun generateExtendedEnum() {
        val schema = """
            type Query {
                people: [Person]
            }

            enum EmployeeTypes {
                ENGINEER
                MANAGER
                DIRECTOR
            }
            
            extend enum EmployeeTypes {
                QA
            }
        """.trimIndent()

        val (_, _, enumTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = enumTypes[0].members[0] as TypeSpec

        // Check generated enum type
        assertThat(type.name).isEqualTo("EmployeeTypes")
        assertThat(type.enumConstants.size).isEqualTo(4)
        assertThat(type.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR", "QA")

        assertCompilesKotlin(enumTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(Pair("Date", "java.time.LocalDateTime"))
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "birthDate")
        assertThat(type.propertySpecs[2].type.toString()).isEqualTo("java.time.LocalDateTime?")

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(type.propertySpecs.size).isEqualTo(1)
        assertThat(type.propertySpecs).extracting("name").contains("genre")

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to (if (genre == null) null else "\"" + genre.toString().replace("\"", "\\\"") + "\""),
            "rating" to rating,
            "views" to views,
            "stars" to stars,
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to ("\"" + genre.toString().replace("\"", "\\\"") + "\""),
            "rating" to rating,
            "views" to views,
            "stars" to stars,
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to ("\"" + genre.toString().replace("\"", "\\\"") + "\""),
            "rating" to rating,
            "average" to average,
            "viewed" to viewed,
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to (if (genre == null) null else "\"" + genre.toString().replace("\"", "\\\"") + "\""),
            "rating" to rating,
            "average" to average,
            "viewed" to viewed,
            "identifier" to (if (identifier == null) null else "\"" + identifier.toString().replace("\"", "\\\"") + "\""),
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun generateToStringMethodForStringWithQuotes() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }

            input MovieFilter {
                genre: String
                id: String!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to (if (genre == null) null else "\"" + genre.toString().replace("\"", "\\\"") + "\""),
            "id" to ("\"" + id.toString().replace("\"", "\\\"") + "\""),
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        var expectedInputString = """
            return linkedMapOf(
            "genre" to serializeListOfString(genre),
            "actors" to serializeListOfString(actors),
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
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

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return linkedMapOf(
            "genre" to genre,
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return linkedMapOf(
            "localDateTime" to (if (localDateTime == null) null else "\"" + localDateTime.toString().replace("\"", "\\\"") + "\""),
            "localDate" to (if (localDate == null) null else "\"" + localDate.toString().replace("\"", "\\\"") + "\""),
            "localTime" to (if (localTime == null) null else "\"" + localTime.toString().replace("\"", "\\\"") + "\""),
            "dateTime" to (if (dateTime == null) null else "\"" + dateTime.toString().replace("\"", "\\\"") + "\""),
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return linkedMapOf(
            "names" to serializeListOfString(names),
            "localDateTime" to serializeListOfLocalDateTime(localDateTime),
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN,
                typeMapping = mapOf(Pair("LocalTime", "java.time.LocalDateTime"), Pair("LocalDate", "java.lang.Integer"))
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.funSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return linkedMapOf(
            "time" to (if (time == null) null else "\"" + time.toString().replace("\"", "\\\"") + "\""),
            "date" to date,
            ).map { it.key + ":" + it.value }.joinToString(",", "{", "}")
        """.trimIndent()
        val generatedInputString = type.funSpecs.single { it.name == "toString" }.body.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)

        assertCompilesKotlin(dataTypes)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")

        assertThat(type.propertySpecs.size).isEqualTo(2)
        assertThat(type.propertySpecs).extracting("name").contains("genre", "releaseYear")

        assertCompilesKotlin(dataTypes)
    }

    @ParameterizedTest(name = "{index} => Snake Case? {0}; expected names {1}")
    @MethodSource("generateConstantsArguments")
    fun `Generates constants from Type names available via the DgsConstants class`(
        snakeCaseEnabled: Boolean,
        constantNames: List<String>,
    ) {
        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                firstname: String
                lastname: String
                metadata: PersonMetaData
            }
            type PersonMetaData { data: [String] }
            type VPersonMetaData { data: [String] }
            type V1PersonMetaData { data: [String] }
            type URLMetaData { data: [String] }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                snakeCaseConstantNames = snakeCaseEnabled
            )
        ).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactlyElementsOf(constantNames)
        assertThat(type.typeSpecs[0].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "People")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Metadata")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Data")

        assertCompilesKotlin(result.dataTypes + result.constants)
    }

    companion object {
        @JvmStatic
        fun generateConstantsArguments(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    true,
                    listOf("QUERY", "PERSON", "PERSON_META_DATA", "V_PERSON_META_DATA", "V_1_PERSON_META_DATA", "URL_META_DATA")
                ),
                Arguments.of(
                    false,
                    listOf("QUERY", "PERSON", "PERSONMETADATA", "VPERSONMETADATA", "V1PERSONMETADATA", "URLMETADATA")
                ),
            )
        }
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email")

        assertCompilesKotlin(result.dataTypes + result.constants)
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email", "BirthYear")

        assertCompilesKotlin(result.dataTypes + result.constants)
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Email")

        assertCompilesKotlin(result.dataTypes + result.constants)
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val type = result.constants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "People", "Friends")

        assertCompilesKotlin(result.dataTypes + result.queryTypes + result.constants)
    }

    @Test
    fun extendSubProjectionOutOfOrder() {
        val schema = """
          type Query {
            search: [SearchResult]
          }

          type SearchResult {
            movie: Movie
          }

          extend type Movie {
            director: String
          }

          type Movie {
            title: String
          }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf(Pair("Long", "java.lang.Long")),
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        val projections = result.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat(projections[1].name).isEqualTo("Search_MovieProjection")
        assertThat((projections[1].members[0] as TypeSpec).funSpecs.map { it.name }).contains("title", "director")

        assertCompilesKotlin(result.clientProjections.plus(result.queryTypes).plus(result.dataTypes).plus(result.enumTypes))
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

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Movie", "Actor")
        assertThat(interfaces).extracting("name").containsExactly("SearchResult")
        val typeSpec = dataTypes[0].members[0] as TypeSpec

        assertThat(typeSpec.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.SearchResult"))

        assertCompilesKotlin(dataTypes + interfaces)
    }

    @Test
    fun generateExtendedUnion() {
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
            
            type Rating {
                stars: Int
            }
            
            extend union SearchResult = Rating
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        assertThat(interfaces.size).isEqualTo(1)

        Truth.assertThat(interfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonSubTypes
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |
                |@JsonTypeInfo(
                |  use = JsonTypeInfo.Id.NAME,
                |  include = JsonTypeInfo.As.PROPERTY,
                |  property = "__typename"
                |)
                |@JsonSubTypes(value = [
                |  JsonSubTypes.Type(value = Movie::class, name = "Movie"),
                |  JsonSubTypes.Type(value = Actor::class, name = "Actor"),
                |  JsonSubTypes.Type(value = Rating::class, name = "Rating")
                |])
                |public interface SearchResult

        """.trimMargin()
        )

        assertCompilesKotlin(dataTypes + interfaces)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Person")

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun skipCodegenOnFields() {
        val schema = """
            type Person {
                name: String
                email: String @skipcodegen
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult
        assertThat(dataTypes).extracting("name").containsExactly("Person")
        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.propertySpecs).extracting("name").containsExactly("name")

        assertCompilesKotlin(dataTypes)
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

        assertCompilesKotlin(dataTypes)
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
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    language = Language.KOTLIN
                )
            ).generate() as KotlinCodeGenResult

        val type = dataTypes[0].members[0] as TypeSpec

        // Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Talent")
        assertThat(type.propertySpecs.size).isEqualTo(4)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company", "imdbProfile")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.superinterfaces.keys)
            .contains(
                ClassName
                    .bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.Employee")
            )

        // Check interface
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
                |""".trimMargin()
        )

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
                |""".trimMargin()
        )

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
                |) : Employee {
                |  public companion object
                |}
                |""".trimMargin()
        )

        assertCompilesKotlin(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithInterfaceFields() {
        // schema contains nullable, non-nullable and list types as interface fields  and fields that are
        // not interfaces
        val schema = """
            interface Pet {
                id: ID!
	            name: String
                address: [String!]!
                mother: Pet!
                father: Pet
            	parents: [Pet]
             }
            type Dog implements Pet {
                id: ID!
	            name: String
                address: [String!]!
                mother: Dog!
                father: Dog
            	parents: [Dog]
            }
            type Bird implements Pet {
                id: ID!
	            name: String
                address: [String!]!
                mother: Bird!
                father: Bird
            	parents: [Bird]
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        Truth.assertThat(interfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import com.fasterxml.jackson.`annotation`.JsonSubTypes
                |import com.fasterxml.jackson.`annotation`.JsonTypeInfo
                |import kotlin.String
                |import kotlin.collections.List
                |
                |@JsonTypeInfo(
                |  use = JsonTypeInfo.Id.NAME,
                |  include = JsonTypeInfo.As.PROPERTY,
                |  property = "__typename"
                |)
                |@JsonSubTypes(value = [
                |  JsonSubTypes.Type(value = Dog::class, name = "Dog"),
                |  JsonSubTypes.Type(value = Bird::class, name = "Bird")
                |])
                |public interface Pet {
                |  public val id: String
                |
                |  public val name: String?
                |
                |  public val address: List<String>
                |
                |  public val mother: Pet
                |
                |  public val father: Pet?
                |
                |  public val parents: List<Pet?>?
                |}
                |""".trimMargin()
        )
        assertCompilesKotlin(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithInterfaceFieldsOfDifferentType() {
        // schema contains nullable, non-nullable and list types as interface fields  and fields that are
        // not interfaces
        val schema = """
            interface Pet {
	            name: String
                diet: Diet
             }
             
            interface Diet {
                calories: String
            }
            
            type Vegetarian implements Diet {
                calories: String
                vegetables: [String]
            }
            
            type Dog implements Pet {
	            name: String
                diet: Vegetarian
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate() as KotlinCodeGenResult

        Truth.assertThat(interfaces[0].toString()).isEqualTo(
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
                |  JsonSubTypes.Type(value = Dog::class, name = "Dog")
                |])
                |public interface Pet {
                |  public val name: String?
                |
                |  public val diet: Diet?
                |}
            |""".trimMargin()
        )
        assertCompilesKotlin(dataTypes + interfaces)
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

        assertCompilesKotlin(codeGenResult.dataTypes + codeGenResult.queryTypes)
    }

    @Test
    fun generateDataTypeThatOmitsNulls() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                mandatoryString: String!
                optionalString: String
                mandatoryInt: Int!
                optionalInt: Int
                mandatoryList: [String]!
                optionalList: [Int]
            }
        """.trimIndent()

        val (dataTypes) =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    language = Language.KOTLIN,
                    omitNullInputFields = true,
                )
            ).generate() as KotlinCodeGenResult

        val movieFilter = compileAndGetConstructor(dataTypes, "MovieFilter")

        assertThat(movieFilter.create("hi", "bye", 3, 7, listOf("hey"), listOf(1, 2)).toString())
            .isEqualTo("""{mandatoryString:"hi",optionalString:"bye",mandatoryInt:3,optionalInt:7,mandatoryList:["hey"],optionalList:[1, 2]}""")
        assertThat(movieFilter.create("hi", null, 3, null, listOf("hey"), null).toString())
            .isEqualTo("""{mandatoryString:"hi",mandatoryInt:3,mandatoryList:["hey"]}""")

        assertCompilesKotlin(dataTypes)
    }

    private fun compileAndGetConstructor(dataTypes: List<FileSpec>, type: String): ClassConstructor {
        val buildDir = assertCompilesKotlin(dataTypes)

        val clazz = URLClassLoader(arrayOf(buildDir.toUri().toURL())).loadClass("$basePackageName.types.$type")
        return ClassConstructor(clazz)
    }
}
