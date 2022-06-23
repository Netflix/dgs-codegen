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

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.junit.jupiter.params.provider.Arguments.arguments
import java.util.stream.Stream
import java.util.stream.Stream.of

class KotlinCodeGenTest {

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                kotlinAllFieldsOptional = true
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                kotlinAllFieldsOptional = true,
            )
        ).generate().kotlinDataTypes

        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].type.toString()).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.types.OtherType?")
        assertThat(type.propertySpecs[0].type.isNullable).isTrue

        assertThat(type.primaryConstructor!!.parameters[0].defaultValue).isNotNull

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun `interface type with kotlinAllFieldsOptional setting`() {

        val schema = """
            interface Person {
                 name: String
            }

            type People implements Person {
                 name: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                kotlinAllFieldsOptional = true,
            )
        ).generate()
        val dataTypes = result.kotlinDataTypes
        val interfaceTypes = result.kotlinInterfaces

        val type = dataTypes[0].members[0] as TypeSpec
        assertThat(type.propertySpecs[0].type.isNullable).isTrue
        val interfaceType = interfaceTypes[0].members[0] as TypeSpec
        assertThat(interfaceType.propertySpecs[0].type.isNullable).isTrue

        assertCompilesKotlin(dataTypes + interfaceTypes)
    }

    @Test
    fun `interface classes are not generated with generateDataTypes setting set to false`() {

        val schema = """
            interface Person {
                 name: String
            }

            type People implements Person {
                 name: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateDataTypes = false,
            )
        ).generate()
        val dataTypes = result.kotlinDataTypes
        val interfaceTypes = result.kotlinInterfaces

        assertThat(dataTypes.size).isEqualTo(0)
        assertThat(interfaceTypes.size).isEqualTo(0)
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = "com.mypackage",
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces
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

        assertThat(FileSpec.get("$basePackageName.types", interfaceType).toString()).isEqualTo(
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
                |
                |  public companion object
                |}
                |""".trimMargin()
        )

        assertThat(FileSpec.get("$basePackageName.types", type).toString()).isEqualTo(
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

        val interfaces = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinInterfaces

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces
        val type = dataTypes[0].members[0] as TypeSpec

        // Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(type.name).isEqualTo("Employee")
        assertThat(type.propertySpecs.size).isEqualTo(3)
        assertThat(type.propertySpecs).extracting("name").contains("firstname", "lastname", "company")
        assertThat(type.primaryConstructor?.parameters?.get(0)?.modifiers).contains(KModifier.OVERRIDE)
        assertThat(type.primaryConstructor?.parameters?.get(0)?.type?.isNullable).isFalse
        assertThat(type.primaryConstructor?.parameters?.get(1)?.type?.isNullable).isFalse
        assertThat(type.primaryConstructor?.parameters?.get(2)?.type?.isNullable).isTrue

        // Check interface
        assertThat(interfaces.size).isEqualTo(1)
        val interfaceType = interfaces[0].members[0] as TypeSpec
        assertThat(interfaceType.name).isEqualTo("Person")
        assertThat(interfaceType.propertySpecs.size).isEqualTo(3)
        assertThat(interfaceType.propertySpecs[0].type.isNullable).isFalse
        assertThat(interfaceType.propertySpecs[1].type.isNullable).isFalse
        assertThat(interfaceType.propertySpecs[2].type.isNullable).isTrue

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

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
        ).generate()
        val type = result.kotlinEnumTypes[0].members[0] as TypeSpec

        // Check generated enum type
        assertThat(type.name).isEqualTo("EmployeeTypes")
        assertThat(type.enumConstants.size).isEqualTo(3)
        assertThat(type.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")
        assertThat(type.typeSpecs[0].isCompanion).isTrue

        assertCompilesKotlin(result.kotlinDataTypes + result.kotlinEnumTypes)
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

        val enumTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinEnumTypes
        val type = enumTypes[0].members[0] as TypeSpec

        // Check generated enum type
        assertThat(type.name).isEqualTo("EmployeeTypes")
        assertThat(type.enumConstants.size).isEqualTo(4)
        assertThat(type.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR", "QA")

        assertCompilesKotlin(enumTypes)
    }

    @Test
    fun `Use mapped type name when the type is mapped`() {

        val schema = """
            type Query {                
                search: SearchResult
            }
            
            type SearchResult {
                person: Person
            }
            
            type Person {
                firstname: String
                lastname: String
                birthDate: Date
            }
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf("Person" to "mypackage.Person"),
            )
        ).generate().kotlinDataTypes

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat((dataTypes[0].members[0] as TypeSpec).propertySpecs[0].type.toString()).isEqualTo("mypackage.Person?")
    }

    class MappedTypesTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = of(
            arguments("java.time.LocalDateTime", "java.time.LocalDateTime"),
            arguments("String", "kotlin.String"),
            arguments("BigDecimal", "java.math.BigDecimal"),
            arguments("Map<*, *>", "kotlin.collections.Map<*, *>"),
            arguments("List<*>", "kotlin.collections.List<*>"),
            arguments(
                "Map<List<String>, Map<String, Any?>>",
                "kotlin.collections.Map<kotlin.collections.List<kotlin.String>, kotlin.collections.Map<kotlin.String, kotlin.Any?>>",
            ),
            arguments(
                "List<List<String>>",
                "kotlin.collections.List<kotlin.collections.List<kotlin.String>>",
            ),
            arguments("kotlin.String", "kotlin.String"),
            arguments("kotlin.collections.Map<*, *>", "kotlin.collections.Map<*, *>"),
            arguments("kotlin.collections.List<*>", "kotlin.collections.List<*>"),
            arguments(
                "kotlin.collections.List<kotlin.collections.List<String?>?>",
                "kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>"
            ),
            arguments(
                "kotlin.collections.List<kotlin.collections.List<kotlin.collections.List<String?>?>?>",
                "kotlin.collections.List<kotlin.collections.List<kotlin.collections.List<kotlin.String?>?>?>"
            ),
            arguments(
                "kotlin.collections.Map<kotlin.collections.List<*>, kotlin.collections.Map<String, Any?>>",
                "kotlin.collections.Map<kotlin.collections.List<*>, kotlin.collections.Map<kotlin.String, kotlin.Any?>>",
            ),
            arguments(
                "Map<Map<String, Any>, Map<String, Any?>>",
                "kotlin.collections.Map<kotlin.collections.Map<kotlin.String, kotlin.Any>, kotlin.collections.Map<kotlin.String, kotlin.Any?>>",
            ),
        )
    }

    @Nested
    inner class GenerateDataClassesWithMappedTypes {
        private val schema = """
            type Query {
                data: JSON
                person: Person
            }
            
            type Person {
                firstname: String
                data: JSON
                dataNotNullable: JSON!
            }
        """.trimIndent()

        @Nested
        inner class ValidCases {
            @ParameterizedTest
            @ArgumentsSource(MappedTypesTestCases::class)
            fun verifyValidCase(given: String, expected: String) {
                val generated = CodeGen(
                    CodeGenConfig(
                        schemas = setOf(schema),
                        packageName = basePackageName,
                        language = Language.KOTLIN,
                        typeMapping = mapOf("JSON" to given),
                    )
                ).generate()
                val dataTypes = generated.kotlinDataTypes

                assertThat(dataTypes).hasSize(1)

                val personType = dataTypes[0].members[0] as TypeSpec

                assertThat(personType.name).isEqualTo("Person")
                assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

                assertThat(personType.propertySpecs).hasSize(3)
                assertThat(personType.propertySpecs).extracting("name")
                    .contains("firstname", "data", "dataNotNullable")

                assertThat(personType.propertySpecs[1].type.toString()).isEqualTo(
                    "$expected?"
                )
                assertThat(personType.propertySpecs[2].type.toString()).isEqualTo(
                    expected
                )

                assertCompilesKotlin(dataTypes)
            }
        }

        @Nested
        inner class WrongCases {
            @ParameterizedTest
            @ValueSource(
                strings = [
                    "",
                    "*",
                    "?",
                    "kotlin.collections.Map<",
                    "kotlin.collections.Map>",
                    "kotlin.collections.Map<>",
                    "kotlin.collections.Map<String, kotlin.collections.ArrayList<>",
                    "kotlin.collections.Map<<String, kotlin.collections.ArrayList<String>>",
                    "kotlin.collections.Map<String, kotlin.collections.ArrayList<>>>",
                    "kotlin.collections.Map<**>",
                ]
            )
            fun testWrongCase(mappedTypeAsString: String) {
                assertThrows<IllegalArgumentException> {
                    CodeGen(
                        CodeGenConfig(
                            schemas = setOf(schema),
                            packageName = basePackageName,
                            language = Language.KOTLIN,
                            typeMapping = mapOf("JSON" to mappedTypeAsString),
                        )
                    ).generate()
                }
            }
        }
    }

    @Test
    fun `Use mapped type name when the type is mapped for interface`() {

        val schema = """
            type Query {                
                search: SearchResult
            }
            
            type SearchResult {
                item: SomethingWithAName
            }
            
            interface SomethingWithAName {
                name: String
            }
            
            type Person implements SomethingWithAName {
                name: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(
                    "SomethingWithAName" to "mypackage.SomethingWithAName",
                    "Person" to "mypackage.Person",
                ),
            )
        ).generate()

        val dataTypes = result.kotlinDataTypes
        val interfaces = result.kotlinInterfaces

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat((dataTypes[0].members[0] as TypeSpec).propertySpecs[0].type.toString()).isEqualTo("mypackage.SomethingWithAName?")

        assertThat(interfaces).isEmpty()
    }

    @Test
    fun `Use mapped type name when the type is mapped for union`() {

        val schema = """
            type Query {                
                search: SearchResult
            }
            
            union SearchResult = Actor | Movie
                     
            type Movie {
                title: String
            }
            
            type Actor {
                name: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(
                    "SearchResult" to "mypackage.SearchResult",
                    "Movie" to "mypackage.Movie",
                    "Actor" to "mypackage.Actor",
                ),
            )
        ).generate()

        val dataTypes = result.kotlinDataTypes
        val interfaces = result.kotlinInterfaces

        assertThat(dataTypes).isEmpty()
        assertThat(interfaces).isEmpty()
    }

    @Test
    fun `Use mapped type name for enum`() {

        val schema = """
            type Query {                
                state: State
            }
            
           enum State {
                ACTIVE, 
                TERMINATED
           }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(
                    "State" to "mypackage.State",
                ),
            )
        ).generate()

        assertThat(result.kotlinDataTypes).isEmpty()
        assertThat(result.kotlinInterfaces).isEmpty()
    }

    @Test
    fun `Use mapped type name when a concrete type of a union is mapped`() {

        val schema = """
            type Query {                
                search: SearchResult
            }
            
            union SearchResult = Actor | Movie
                     
            type Movie {
                title: String
            }
            
            type Actor {
                name: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                typeMapping = mapOf(
                    "Actor" to "mypackage.Actor",
                ),
            )
        ).generate()
        val dataTypes = result.kotlinDataTypes
        val interfaces = result.kotlinInterfaces

        assertThat(dataTypes).hasSize(1)
        assertThat(interfaces).hasSize(1)
        assertThat((dataTypes[0].members[0] as TypeSpec).name).isEqualTo("Movie")
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val enums = codeGenResult.kotlinEnumTypes
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

        assertCompilesKotlin(dataTypes.plus(enums))
    }

    @Test
    fun generateInputWithEmptyDefaultValueForArray() {
        val schema = """
            input SomeType {
                names: [String!]! = []
            }
        """.trimIndent()

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate().kotlinDataTypes
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val enums = codeGenResult.kotlinEnumTypes
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

        assertCompilesKotlin(dataTypes.plus(enums))
    }

    @Test
    fun generateInputWithDefaultValueForBooleanArray() {
        val schema = """
            input SomeType {
                booleans: [Boolean!]! = [true]
            }          
        """.trimIndent()

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, language = Language.KOTLIN)).generate().kotlinDataTypes
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val type = dataTypes[0].members[0] as TypeSpec

        assertThat(type.name).isEqualTo("MovieFilter")

        assertThat(type.propertySpecs.size).isEqualTo(2)
        assertThat(type.propertySpecs).extracting("name").contains("genre", "releaseYear")

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun `Should be able to generate successfully when java keywords and default value are used as input types`() {
        val schema = """
            type Query {
                foo(fooInput: FooInput): Baz
                bar(barInput: BarInput): Baz
            }
            
            input FooInput {
                public: Boolean = true
            }
            
            input BarInput {
                public: Boolean
            }

            type Baz {
                public: Boolean
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN,
                generateDataTypes = false,
                generateClientApi = true,
                includeQueries = setOf("foo", "bar")
            )
        ).generate()

        assertThat(codeGenResult.kotlinDataTypes.size).isEqualTo(2)

        val fileSpec0 = codeGenResult.kotlinDataTypes[0] as FileSpec
        assertThat(fileSpec0.name).isEqualTo("FooInput")
        assertThat(fileSpec0.members.size).isEqualTo(1)
        val typeSpec0 = fileSpec0.members[0] as TypeSpec
        assertThat(typeSpec0.propertySpecs.size).isEqualTo(1)
        assertThat(typeSpec0.propertySpecs[0].name).isEqualTo("public")

        val fileSpec1 = codeGenResult.kotlinDataTypes[1] as FileSpec
        assertThat(fileSpec1.name).isEqualTo("BarInput")
        assertThat(fileSpec1.members.size).isEqualTo(1)
        val typeSpec1 = fileSpec1.members[0] as TypeSpec
        assertThat(typeSpec1.propertySpecs.size).isEqualTo(1)
        assertThat(typeSpec1.propertySpecs[0].name).isEqualTo("public")

        assertCompilesKotlin(codeGenResult.kotlinDataTypes)
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
        ).generate()
        val type = result.kotlinConstants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactlyElementsOf(constantNames)
        assertThat(type.typeSpecs[0].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "People")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Metadata")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Data")

        assertCompilesKotlin(result.kotlinDataTypes + result.kotlinConstants)
    }

    companion object {
        @JvmStatic
        fun generateConstantsArguments(): Stream<Arguments> {
            return of(
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
        ).generate()
        val type = result.kotlinConstants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email")

        assertCompilesKotlin(result.kotlinDataTypes + result.kotlinConstants)
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
        ).generate()
        val type = result.kotlinConstants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Email", "BirthYear")

        assertCompilesKotlin(result.kotlinDataTypes + result.kotlinConstants)
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
        ).generate()
        val type = result.kotlinConstants[0].members[0] as TypeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[1].propertySpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Email")

        assertCompilesKotlin(result.kotlinDataTypes + result.kotlinConstants)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(interfaces.size).isEqualTo(1)

        assertThat(interfaces[0].toString()).isEqualTo(
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
                |public interface SearchResult {
                |  public companion object
                |}

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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes
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

        val dataTypes = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, subPackageNameTypes = "mytypes", language = Language.KOTLIN)).generate().kotlinDataTypes

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("$basePackageName.mytypes")

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun unionTypesWithoutInterfaceCanDeserialize() {
        val schema = """
            type Query {
                search(text: String!): SearchResultPage
            }

            type Human {
                id: ID!
                name: String!
                totalCredits: Int
            }

            type Droid {
                id: ID!
                name: String!
                primaryFunction: String
            }

            union SearchResult = Human | Droid

            type SearchResultPage {
                items: [SearchResult]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(dataTypes.size).isEqualTo(3) // human, droid, searchresultpage

        val human = dataTypes[0].members[0] as TypeSpec
        assertThat(human.name).isEqualTo("Human")
        assertThat(human.propertySpecs.size).isEqualTo(3)
        assertThat(human.propertySpecs).extracting("name").contains("id", "name", "totalCredits")
        assertThat(human.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.SearchResult"))

        val droid = dataTypes[1].members[0] as TypeSpec
        assertThat(droid.name).isEqualTo("Droid")
        assertThat(droid.propertySpecs.size).isEqualTo(3)
        assertThat(droid.propertySpecs).extracting("name").contains("id", "name", "primaryFunction")
        assertThat(droid.superinterfaces.keys).contains(ClassName.bestGuess("com.netflix.graphql.dgs.codegen.tests.generated.types.SearchResult"))

        val rsultPage = dataTypes[2].members[0] as TypeSpec
        assertThat(rsultPage.name).isEqualTo("SearchResultPage")
        assertThat(rsultPage.propertySpecs.size).isEqualTo(1)
        assertThat(rsultPage.propertySpecs).extracting("name").contains("items")
        assertThat(rsultPage.superinterfaces.keys).isEmpty()

        assertThat(interfaces.size).isEqualTo(1)
        val searchResult = interfaces[0].members[0] as TypeSpec
        assertThat(FileSpec.get("$basePackageName.types", searchResult).toString()).contains(
            """
                |@JsonTypeInfo(
                |  use = JsonTypeInfo.Id.NAME,
                |  include = JsonTypeInfo.As.PROPERTY,
                |  property = "__typename"
                |)
                |@JsonSubTypes(value = [
                |  JsonSubTypes.Type(value = Human::class, name = "Human"),
                |  JsonSubTypes.Type(value = Droid::class, name = "Droid")
                |])
                |""".trimMargin()
        )

        // This ensures deserializability in the absence of __typename
        assertThat(FileSpec.get("$basePackageName.types", human).toString()).contains("@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)")
    }

    @Test
    fun generateInterfaceWithInterfaceInheritance() {
        val schema = """
            type Query {
                fruits: [Fruit]
            }
            
            type Seed {
                name: String
            }

            interface Fruit {
              seeds: [Seed]
            }
            
            interface StoneFruit implements Fruit {
              seeds: [Seed]
              fuzzy: Boolean
            }

        """.trimIndent()

        val codeGenResult =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    language = Language.KOTLIN
                )
            ).generate()

        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(dataTypes).hasSize(1)
        assertThat(interfaces).hasSize(2)

        val stoneFruitInterface = (interfaces[1].members[0] as TypeSpec)
        assertThat(stoneFruitInterface.superinterfaces).hasSize(1)
        assertThat((stoneFruitInterface.superinterfaces.entries.first().key as ClassName).simpleName).isEqualTo("Fruit")

        assertCompilesKotlin(dataTypes + interfaces)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

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
        assertThat(FileSpec.get("$basePackageName.types", personInterfaceType).toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types
                |
                |import kotlin.String
                |
                |public interface Person {
                |  public val firstname: String?
                |
                |  public val lastname: String?
                |
                |  public companion object
                |}
                |""".trimMargin()
        )

        val employeeInterfaceType = interfaces[1].members[0] as TypeSpec
        assertThat(FileSpec.get("$basePackageName.types", employeeInterfaceType).toString()).isEqualTo(
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
                |public interface Employee : Person {
                |  public override val firstname: String?
                |
                |  public override val lastname: String?
                |
                |  public val company: String?
                |
                |  public companion object
                |}
                |""".trimMargin()
        )

        assertThat(FileSpec.get("$basePackageName.types", type).toString()).isEqualTo(
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(interfaces[0].toString()).isEqualTo(
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
                |
                |  public companion object
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()
        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(interfaces[0].toString()).isEqualTo(
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
                |
                |  public companion object
                |}
            |""".trimMargin()
        )
        assertCompilesKotlin(dataTypes + interfaces)
    }

    @Test
    fun generateClassKDoc() {

        val schema = """
            type Query {
                search(movieFilter: MovieFilter!): Movie
            }

            ""${'"'}
            Movies are fun to watch.
            They also work well as examples in GraphQL.
            ""${'"'}
            type Movie {
                title: String
            }
            
            ""${'"'}
            Example filter for Movies.
            
            It takes a title and such.
            ""${'"'}
            input MovieFilter {
                titleFilter: String
            }
            
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

        val type = dataTypes[0].members[0] as TypeSpec
        val inputType = dataTypes[1].members[0] as TypeSpec

        assertThat(type.kdoc.toString()).isEqualTo(
            """Movies are fun to watch.
They also work well as examples in GraphQL.
            """.trimIndent()
        )

        assertThat(inputType.kdoc.toString()).isEqualTo(
            """Example filter for Movies.

It takes a title and such.
            """.trimIndent()
        )

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun generateClassFieldsKDoc() {

        val schema = """
            type Query {
                search(movieFilter: MovieFilter!): Movie
            }
        
            type Movie {
                ""${'"'}
                The original, non localized title with some specials characters : %!({[*$,.:;.
                ""${'"'}
                title: String
            }
                 
            input MovieFilter {
                ""${'"'}
                Starts-with filter
                ""${'"'}
                titleFilter: String
            }
            
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinDataTypes

        val type = dataTypes[0].members[0] as TypeSpec
        val inputType = dataTypes[1].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].kdoc.toString()).isEqualTo(
            """The original, non localized title with some specials characters : %!({[*$,.:;.
            """.trimIndent()
        )

        assertThat(inputType.propertySpecs[0].kdoc.toString()).isEqualTo(
            """Starts-with filter
            """.trimIndent()
        )

        assertCompilesKotlin(dataTypes)
    }

    @Test
    fun generateInterfaceKDoc() {
        val schema = """           
            ""${'"'}
            Anything with a title!
            ""${'"'}
            interface Titled {
                title: String
            }                                 
        """.trimIndent()

        val interfaces = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinInterfaces

        val type = interfaces[0].members[0] as TypeSpec

        assertThat(type.kdoc.toString()).isEqualTo(
            """Anything with a title!
            """.trimIndent()
        )

        assertCompilesKotlin(interfaces)
    }

    @Test
    fun generateInterfaceFieldsKDoc() {
        val schema = """                       
            interface Titled {
               ""${'"'}
                The original, non localized title.
                ""${'"'}
                title: String
            }                                 
        """.trimIndent()

        val interfaces = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinInterfaces

        val type = interfaces[0].members[0] as TypeSpec

        assertThat(type.propertySpecs[0].kdoc.toString()).isEqualTo(
            """The original, non localized title.
            """.trimIndent()
        )

        assertCompilesKotlin(interfaces)
    }

    @Test
    fun `can generate code based on GraphQL interfaces, which fields express java key-words`() {
        val schema = """
            type Query {
              queryRoot: QueryRoot
            }

            interface HasDefaultField {
              default: String
              public: String
              private: Boolean
            }
            
            type QueryRoot implements HasDefaultField {
                name: String
                default: String
                public: String
                private: Boolean
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate()

        assertThat(codeGenResult.kotlinDataTypes.size).isEqualTo(1)
        assertThat(codeGenResult.kotlinDataTypes[0].name).isEqualTo("QueryRoot")
        assertThat(codeGenResult.kotlinDataTypes[0].members.size).isEqualTo(1)

        val dataTypeSpec = codeGenResult.kotlinDataTypes[0].members[0] as TypeSpec

        assertThat(dataTypeSpec.propertySpecs.size).isEqualTo(4)
        assertThat(dataTypeSpec.propertySpecs[0].name).isEqualTo("name")
        assertThat(dataTypeSpec.propertySpecs[1].name).isEqualTo("default")
        assertThat(dataTypeSpec.propertySpecs[2].name).isEqualTo("public")
        assertThat(dataTypeSpec.propertySpecs[3].name).isEqualTo("private")

        val interfaces = codeGenResult.kotlinInterfaces

        assertThat(interfaces.size).isEqualTo(1)

        val interfaceTypeSpec = interfaces[0].members[0] as TypeSpec

        assertThat(interfaceTypeSpec.propertySpecs.size).isEqualTo(3)
        assertThat(interfaceTypeSpec.propertySpecs[0].name).isEqualTo("default")
        assertThat(interfaceTypeSpec.propertySpecs[1].name).isEqualTo("public")
        assertThat(interfaceTypeSpec.propertySpecs[2].name).isEqualTo("private")

        assertCompilesKotlin(codeGenResult)
    }

    @Test
    fun generateEnumKDoc() {
        val schema = """           
            ""${'"'}
            Some options
            ""${'"'}
            enum Color {
                red,white,blue
            }                                 
        """.trimIndent()

        val enums = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinEnumTypes

        val type = enums[0].members[0] as TypeSpec

        assertThat(type.kdoc.toString()).isEqualTo(
            """Some options
            """.trimIndent()
        )

        assertCompilesKotlin(enums)
    }

    @Test
    fun generateEnumItemKDoc() {
        val schema = """                       
            enum Color {
                ""${'"'}
                The first option
                ""${'"'}
                red,
                
                ""${'"'}
                The second option
                ""${'"'}
                blue                         
            }                                 
        """.trimIndent()

        val enums = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN
            )
        ).generate().kotlinEnumTypes

        val type = enums[0].members[0] as TypeSpec

        assertThat(type.enumConstants["red"]?.kdoc.toString()).isEqualTo(
            """The first option
            """.trimIndent()
        )

        assertThat(type.enumConstants["blue"]?.kdoc.toString()).isEqualTo(
            """The second option
            """.trimIndent()
        )

        assertCompilesKotlin(enums)
    }
}
