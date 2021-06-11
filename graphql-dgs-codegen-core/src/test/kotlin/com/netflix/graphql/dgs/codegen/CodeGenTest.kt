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

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.google.common.truth.Truth
import com.netflix.graphql.dgs.codegen.generators.java.disableJsonTypeInfoAnnotation
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.WildcardTypeName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files
import java.time.*
import java.util.stream.Stream
import javax.tools.JavaFileObject

class CodeGenTest {

    val clock = Clock.fixed(Instant.ofEpochMilli(0), ZoneId.of("America/Sao_Paulo"))
    val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"
    val typesPackageName = "$basePackageName.types"
    val dataFetcherPackageName = "$basePackageName.datafetchers"

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname")
        assertThat(typeSpec.methodSpecs).flatExtracting("parameters").extracting("name").contains("firstname", "lastname")
        dataTypes[0].writeTo(System.out)
        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("java.lang.Integer")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("java.lang.Boolean")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("java.lang.Double")
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
            )
        ).generate() as CodeGenResult
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("int")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("boolean")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("double")
    }

    @Test
    fun generateBoxedDataClassWithNonNullablePrimitive() {
        val schema = """
            type MyType {
                count: Int!
                truth: Boolean!
                floaty: Float!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateBoxedTypes = true)).generate() as CodeGenResult
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("java.lang.Integer")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("java.lang.Boolean")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("java.lang.Double")
    }

    @Test
    fun generateDataClassWithNonNullablePrimitiveInList() {
        val schema = """
            type MyType {
                count: [Int!]
                truth: [Boolean!]
                floaty: [Float!]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("java.util.List<java.lang.Integer>")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("java.util.List<java.lang.Boolean>")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("java.util.List<java.lang.Double>")
    }

    @Test
    fun generateDataClassWithToString() {

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        val toString = assertThat(dataTypes[0].typeSpec.methodSpecs).filteredOn("name", "toString")
        toString.extracting("code").allMatch { it.toString().contains("return \"Person{") }

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateDataClassWithEquals() {

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("equals")

        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("equals")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateDataClassWithBuilder() {

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("newBuilder")
        val builderType = dataTypes[0].typeSpec.typeSpecs[0]
        assertThat(builderType.name).isEqualTo("Builder")
        assertThat(builderType.methodSpecs).extracting("name").contains("firstname", "lastname", "build")
        assertThat(builderType.methodSpecs).filteredOn("name", "firstname").extracting("returnType.simpleName").contains("com.netflix.graphql.dgs.codegen.tests.generated.types.Person.Builder")
        assertThat(builderType.methodSpecs).filteredOn("name", "build").extracting("returnType.simpleName").contains("Person")
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateDataClassWithHashcode() {

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("hashCode")

        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("com.mypackage.types")

        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("name", "email")
        val type = assertThat(dataTypes[0].typeSpec.fieldSpecs).filteredOn("name", "email").extracting("type")
        type.extracting("rawType.canonicalName").contains("java.util.List")
        type.flatExtracting("typeArguments").extracting("canonicalName").contains("java.lang.String")

        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("name", "email")
        val type = assertThat(dataTypes[0].typeSpec.fieldSpecs).filteredOn("name", "email").extracting("type")
        type.extracting("rawType.canonicalName").contains("java.util.List")
        type.flatExtracting("typeArguments").extracting("canonicalName").contains("java.lang.String")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateInterfaceClassWithNonNullableFields() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            interface Person {
                firstname: String!
                lastname: String
            }
            
            type Employee implements Person {
                firstname: String!
                lastname: String
                company: String
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        // Check data class
        assertThat(employee.name).isEqualTo("Employee")
        assertThat(employee.fieldSpecs.size).isEqualTo(3)
        assertThat(employee.fieldSpecs).extracting("name").contains("firstname", "lastname", "company")

        val annotation = employee.annotations.single()
        Truth.assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        val person = interfaces[0]
        Truth.assertThat(person.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import com.fasterxml.jackson.annotation.JsonSubTypes;
               |import com.fasterxml.jackson.annotation.JsonTypeInfo;
               |import java.lang.String;
               |
               |@JsonTypeInfo(
               |    use = JsonTypeInfo.Id.NAME,
               |    include = JsonTypeInfo.As.PROPERTY,
               |    property = "__typename"
               |)
               |@JsonSubTypes(@JsonSubTypes.Type(value = Employee.class, name = "Employee"))
               |public interface Person {
               |  String getFirstname();
               |
               |  String getLastname();
               |}
               |""".trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
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
                packageName = basePackageName
            )
        ).generate() as CodeGenResult

        Truth.assertThat(interfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonSubTypes;
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |import java.lang.String;
                |import java.util.List;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NAME,
                |    include = JsonTypeInfo.As.PROPERTY,
                |    property = "__typename"
                |)
                |@JsonSubTypes({
                |    @JsonSubTypes.Type(value = Dog.class, name = "Dog"),
                |    @JsonSubTypes.Type(value = Bird.class, name = "Bird")
                |})
                |public interface Pet {
                |  String getId();
                |
                |  String getName();
                |
                |  List<String> getAddress();
                |}
            |""".trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithInterfaceFieldsOfDifferentType() {
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
            )
        ).generate() as CodeGenResult

        Truth.assertThat(interfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonSubTypes;
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |import java.lang.String;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NAME,
                |    include = JsonTypeInfo.As.PROPERTY,
                |    property = "__typename"
                |)
                |@JsonSubTypes(@JsonSubTypes.Type(value = Dog.class, name = "Dog"))
                |public interface Pet {
                |  String getName();
                |}
            |""".trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        // Check data class
        assertThat(employee.name).isEqualTo("Employee")
        assertThat(employee.fieldSpecs.size).isEqualTo(3)
        assertThat(employee.fieldSpecs).extracting("name").contains("firstname", "lastname", "company")

        val annotation = employee.annotations.single()
        Truth.assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        val person = interfaces[0]
        Truth.assertThat(person.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import com.fasterxml.jackson.annotation.JsonSubTypes;
               |import com.fasterxml.jackson.annotation.JsonTypeInfo;
               |import java.lang.String;
               |
               |@JsonTypeInfo(
               |    use = JsonTypeInfo.Id.NAME,
               |    include = JsonTypeInfo.As.PROPERTY,
               |    property = "__typename"
               |)
               |@JsonSubTypes(@JsonSubTypes.Type(value = Employee.class, name = "Employee"))
               |public interface Person {
               |  String getFirstname();
               |
               |  String getLastname();
               |}
               |""".trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
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
            )
        ).generate() as CodeGenResult

        // Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(3)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname", "friends")

        // Check type of friends field
        val parameterizedType = ParameterizedTypeName.get(ClassName.get(List::class.java), ClassName.get(typesPackageName, "Person"))
        assertThat(dataTypes[0].typeSpec.fieldSpecs)
            .withFailMessage("Incorrect type for friends field. List<Person> expected.")
            .filteredOn { it.name == "friends" }
            .extracting("type", ParameterizedTypeName::class.java)
            .contains(parameterizedType)

        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes).extracting("typeSpec.name").contains("Car", "Engine", "Performance")
        assertThat(dataTypes)
            .filteredOn("typeSpec.name", "Engine")
            .extracting("typeSpec")
            .flatExtracting("fieldSpecs")
            .filteredOn("name", "performance")
            .extracting("type.simpleName")
            .containsExactly("Performance")

        assertCompilesJava(dataTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult

        // Check generated enum type
        assertThat(codeGenResult.javaEnumTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.name).isEqualTo("EmployeeTypes")
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants.size).isEqualTo(3)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")

        assertCompilesJava(codeGenResult.javaEnumTypes)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult

        // Check generated enum type
        assertThat(codeGenResult.javaEnumTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.name).isEqualTo("EmployeeTypes")
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants.size).isEqualTo(4)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR", "QA")

        assertCompilesJava(codeGenResult.javaEnumTypes)
    }

    @Test
    fun generateDataFetcherClass() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].typeSpec.name).isEqualTo("PeopleDatafetcher")
        assertThat(dataFetchers[0].packageName).isEqualTo(dataFetcherPackageName)
        assertCompilesJava(dataFetchers + dataTypes)
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
                typeMapping = mapOf(Pair("Date", "java.time.LocalDateTime")),
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(3)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname", "birthDate")
        dataTypes[0].writeTo(System.out)
        assertCompilesJava(dataTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("genre")

        assertCompilesJava(dataTypes)
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

        val (dataTypes, _, enumTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("ColorFilter")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("color")
        assertThat(colorField.type.toString()).isEqualTo("$typesPackageName.Color")
        assertThat(colorField.initializer.toString()).isEqualTo("$typesPackageName.Color.red")

        assertCompilesJava(enumTypes + dataTypes)
    }

    @Test
    fun generateInputWithDefaultValueForArray() {
        val schema = """
            input SomeType {
                names: [String] = []
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("names")
        assertThat(colorField.initializer.toString()).isEqualTo("java.util.Collections.emptyList()")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateInputWithDefaultStringValueForArray() {
        val schema = """
            input SomeType {
                names: [String] = ["A", "B"]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("names")
        assertThat(colorField.initializer.toString()).isEqualTo("""java.util.Arrays.asList("A", "B")""")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateInputWithDefaultIntValueForArray() {
        val schema = """
            input SomeType {
                numbers: [Int] = [1, 2, 3]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("numbers")
        assertThat(colorField.initializer.toString()).isEqualTo("""java.util.Arrays.asList(1, 2, 3)""")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateInputWithDefaultEnumValueForArray() {
        val schema = """
            input SomeType {
                colors: [Color] = [red]
            }
            
            enum Color {
                red,
                blue
            }
        """.trimIndent()

        val (dataTypes, _, enumTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, writeToFiles = true)).generate() as CodeGenResult
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("colors")
        assertThat(colorField.initializer.toString()).isEqualTo("""java.util.Arrays.asList(Color.red)""")

        assertCompilesJava(dataTypes + enumTypes)
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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("genre", "releaseYear")
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateToStringMethodForTypes() {

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
            )
        ).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
        assertCompilesJava(dataTypes)
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
                snakeCaseConstantNames = snakeCaseEnabled,
            )
        ).generate() as CodeGenResult
        val type = result.javaConstants[0].typeSpec
        assertThat(type.name).isEqualTo("DgsConstants")
        assertThat(type.typeSpecs).extracting("name").containsExactlyElementsOf(constantNames)
        assertThat(type.typeSpecs[0].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "People")
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
            )
        ).generate() as CodeGenResult
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "Email")
        assertThat(type.typeSpecs[2].fieldSpecs[0].initializer.toString()).contains("\"PersonFilter\"")
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
            )
        ).generate() as CodeGenResult
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON", "PERSONFILTER")
        assertThat(type.typeSpecs[2].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "Email", "BirthYear")
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
            )
        ).generate() as CodeGenResult
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[1].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname", "Email")
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
            )
        ).generate() as CodeGenResult
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "People", "Friends")
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
            )
        ).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Movie")
        assertThat(dataTypes[1].typeSpec.name).isEqualTo("Actor")
        assertThat(interfaces[0].typeSpec.name).isEqualTo("SearchResult")
        val typeSpec = dataTypes[0]

        assertThat(typeSpec.typeSpec.superinterfaces[0]).isEqualTo(ClassName.get("com.netflix.graphql.dgs.codegen.tests.generated.types", "SearchResult"))
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult
        assertThat(result.javaDataTypes[0].typeSpec.name).isEqualTo("Movie")
        assertThat(result.javaDataTypes[1].typeSpec.name).isEqualTo("Actor")
        assertThat(result.javaDataTypes[2].typeSpec.name).isEqualTo("Rating")
        assertThat(result.javaInterfaces[0].typeSpec.name).isEqualTo("SearchResult")

        Truth.assertThat(result.javaInterfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonSubTypes;
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NAME,
                |    include = JsonTypeInfo.As.PROPERTY,
                |    property = "__typename"
                |)
                |@JsonSubTypes({
                |    @JsonSubTypes.Type(value = Movie.class, name = "Movie"),
                |    @JsonSubTypes.Type(value = Actor.class, name = "Actor"),
                |    @JsonSubTypes.Type(value = Rating.class, name = "Rating")
                |})
                |public interface SearchResult {
                |}
            |""".trimMargin()
        )

        assertCompilesJava(result.javaDataTypes + result.javaInterfaces)
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
            )
        ).generate() as CodeGenResult
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
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
            )
        ).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").containsExactly("name")
    }

    @Test
    fun generateWithCustomSubPackageName() {

        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, subPackageNameTypes = "mytypes")).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("$basePackageName.mytypes")
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateDataClassWithInterfaceInheritance() {

        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String!
                lastname: String
            }

            interface Employee implements Person {
                firstname: String!
                lastname: String
                company: String
            }

            type Talent implements Employee {
                firstname: String!
                lastname: String
                company: String
                imdbProfile: String
            }

        """.trimIndent()

        val (dataTypes, interfaces) =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName
                )
            ).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val talent = dataTypes.single().typeSpec
        // Check data class
        assertThat(talent.name).isEqualTo("Talent")
        assertThat(talent.fieldSpecs.size).isEqualTo(4)
        assertThat(talent.fieldSpecs)
            .extracting("name")
            .contains("firstname", "lastname", "company", "imdbProfile")

        val annotation = talent.annotations.single()
        Truth.assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        assertThat(interfaces).hasSize(2)

        val person = interfaces[0]
        Truth.assertThat(person.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import java.lang.String;
               |
               |public interface Person {
               |  String getFirstname();
               |
               |  String getLastname();
               |}
               |""".trimMargin()
        )

        val employee = interfaces[1]
        Truth.assertThat(employee.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import com.fasterxml.jackson.annotation.JsonSubTypes;
               |import com.fasterxml.jackson.annotation.JsonTypeInfo;
               |import java.lang.String;
               |
               |@JsonTypeInfo(
               |    use = JsonTypeInfo.Id.NAME,
               |    include = JsonTypeInfo.As.PROPERTY,
               |    property = "__typename"
               |)
               |@JsonSubTypes(@JsonSubTypes.Type(value = Talent.class, name = "Talent"))
               |public interface Employee {
               |  String getFirstname();
               |
               |  String getLastname();
               |
               |  String getCompany();
               |}
               |""".trimMargin()
        )

        Truth.assertThat(JavaFile.builder("$basePackageName.types", talent).build().toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |import java.lang.Object;
                |import java.lang.Override;
                |import java.lang.String;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NONE
                |)
                |public class Talent implements com.netflix.graphql.dgs.codegen.tests.generated.types.Employee {
                |  private String firstname;
                |
                |  private String lastname;
                |
                |  private String company;
                |
                |  private String imdbProfile;
                |
                |  public Talent() {
                |  }
                |
                |  public Talent(String firstname, String lastname, String company, String imdbProfile) {
                |    this.firstname = firstname;
                |    this.lastname = lastname;
                |    this.company = company;
                |    this.imdbProfile = imdbProfile;
                |  }
                |
                |  public String getFirstname() {
                |    return firstname;
                |  }
                |
                |  public void setFirstname(String firstname) {
                |    this.firstname = firstname;
                |  }
                |
                |  public String getLastname() {
                |    return lastname;
                |  }
                |
                |  public void setLastname(String lastname) {
                |    this.lastname = lastname;
                |  }
                |
                |  public String getCompany() {
                |    return company;
                |  }
                |
                |  public void setCompany(String company) {
                |    this.company = company;
                |  }
                |
                |  public String getImdbProfile() {
                |    return imdbProfile;
                |  }
                |
                |  public void setImdbProfile(String imdbProfile) {
                |    this.imdbProfile = imdbProfile;
                |  }
                |
                |  @Override
                |  public String toString() {
                |    return "Talent{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'," +"company='" + company + "'," +"imdbProfile='" + imdbProfile + "'" +"}";
                |  }
                |
                |  @Override
                |  public boolean equals(Object o) {
                |    if (this == o) return true;
                |        if (o == null || getClass() != o.getClass()) return false;
                |        Talent that = (Talent) o;
                |        return java.util.Objects.equals(firstname, that.firstname) &&
                |                            java.util.Objects.equals(lastname, that.lastname) &&
                |                            java.util.Objects.equals(company, that.company) &&
                |                            java.util.Objects.equals(imdbProfile, that.imdbProfile);
                |  }
                |
                |  @Override
                |  public int hashCode() {
                |    return java.util.Objects.hash(firstname, lastname, company, imdbProfile);
                |  }
                |
                |  public static com.netflix.graphql.dgs.codegen.tests.generated.types.Talent.Builder newBuilder() {
                |    return new Builder();
                |  }
                |
                |  public static class Builder {
                |    private String firstname;
                |
                |    private String lastname;
                |
                |    private String company;
                |
                |    private String imdbProfile;
                |
                |    public Talent build() {
                |                  com.netflix.graphql.dgs.codegen.tests.generated.types.Talent result = new com.netflix.graphql.dgs.codegen.tests.generated.types.Talent();
                |                      result.firstname = this.firstname;
                |          result.lastname = this.lastname;
                |          result.company = this.company;
                |          result.imdbProfile = this.imdbProfile;
                |                      return result;
                |    }
                |
                |    public com.netflix.graphql.dgs.codegen.tests.generated.types.Talent.Builder firstname(
                |        String firstname) {
                |      this.firstname = firstname;
                |      return this;
                |    }
                |
                |    public com.netflix.graphql.dgs.codegen.tests.generated.types.Talent.Builder lastname(
                |        String lastname) {
                |      this.lastname = lastname;
                |      return this;
                |    }
                |
                |    public com.netflix.graphql.dgs.codegen.tests.generated.types.Talent.Builder company(
                |        String company) {
                |      this.company = company;
                |      return this;
                |    }
                |
                |    public com.netflix.graphql.dgs.codegen.tests.generated.types.Talent.Builder imdbProfile(
                |        String imdbProfile) {
                |      this.imdbProfile = imdbProfile;
                |      return this;
                |    }
                |  }
                |}
                |""".trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateConstantsWithExtendedInterface() {
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
            )
        ).generate() as CodeGenResult

        assertThat(result.javaInterfaces).hasSize(1)
        assertThat(result.javaInterfaces[0].typeSpec.methodSpecs).hasSize(3)
        assertThat(result.javaInterfaces[0].typeSpec.methodSpecs).extracting("name").containsExactly("getFirstname", "getLastname", "getAge")
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
          scalar MovieID @javaType(name : "java.lang.String")
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf())).generate() as CodeGenResult
        assertCompilesJava(codeGenResult.javaFiles)
    }

    @Test
    fun generateObjectTypeInterfaceWithInterfaceInheritance() {
        val schema = """
        
        interface Fruit {
            name: String
        }
        
        type Apple implements Fruit {
            name: String
        }
        
        type Basket {
            fruit: Fruit
        }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true
            )
        ).generate() as CodeGenResult

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val iapple = interfaces[0]
        assertThat(iapple.typeSpec.name).isEqualTo("IApple")
        assertThat(iapple.typeSpec.superinterfaces.size).isEqualTo(1)
        assertThat((iapple.typeSpec.superinterfaces[0] as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(iapple.typeSpec.methodSpecs).extracting("name").containsExactly("getName")

        val ibasket = interfaces[1]
        assertThat(ibasket.typeSpec.name).isEqualTo("IBasket")
        assertThat((ibasket.typeSpec.methodSpecs[0].returnType as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(ibasket.typeSpec.methodSpecs).extracting("name").containsExactly("getFruit")

        val fruit = interfaces[2]
        assertThat(fruit.typeSpec.name).isEqualTo("Fruit")
        assertThat(fruit.typeSpec.methodSpecs).extracting("name").containsExactly("getName")

        val apple = dataTypes[0]
        assertThat(apple.typeSpec.name).isEqualTo("Apple")
        assertThat(apple.typeSpec.superinterfaces.size).isEqualTo(2)
        assertThat(apple.typeSpec.methodSpecs).extracting("name").contains("getName")

        val basket = dataTypes[1]
        assertThat(basket.typeSpec.name).isEqualTo("Basket")
        assertThat((basket.typeSpec.methodSpecs[0].returnType as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(basket.typeSpec.methodSpecs).extracting("name").contains("getFruit")

        assertCompilesJava(dataTypes.plus(interfaces))
    }

    @Test
    fun generateObjectTypeInterfaceWithInterface() {
        val schema = """
        type Team {
            name: String
        }

        type Player {
            name: String
        }

        interface Standing {
            position: Int!
            team: Team!
        }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true
            )
        ).generate() as CodeGenResult

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(3)

        val team = interfaces[0]
        assertThat(team.typeSpec.name).isEqualTo("ITeam")
        assertThat(team.typeSpec.methodSpecs).extracting("name").containsExactly("getName")
        assertThat(team.typeSpec.methodSpecs[0].returnType).extracting("simpleName").containsExactly("String")

        val player = interfaces[1]
        assertThat(player.typeSpec.name).isEqualTo("IPlayer")
        assertThat(player.typeSpec.methodSpecs).extracting("name").containsExactly("getName")
        assertThat(player.typeSpec.methodSpecs[0].returnType).extracting("simpleName").containsExactly("String")

        val standing = interfaces[2]
        assertThat(standing.typeSpec.name).isEqualTo("Standing")
        assertThat(standing.typeSpec.methodSpecs).extracting("name").containsExactly("getPosition", "getTeam")
        assertThat(standing.typeSpec.methodSpecs[0].returnType.toString()).contains("int")
        assertThat(standing.typeSpec.methodSpecs[1].returnType).extracting("simpleName").containsExactly("ITeam")

        assertCompilesJava(dataTypes.plus(interfaces))
    }

    @Test
    fun generateObjectTypeInterface() {
        val schema = """
            type Query {
                movie(id: ID): Movie
                movies(filter: MovieFilter): MoviePage
            }

            input MovieFilter {
                title: String
                genre: Genre
                language: Language
                tags: [String]
            }

            type Movie {
                id: ID
                title: String
                genre: Genre
                language: Language
                tags: [String]
            }

            type MoviePage {
                items: [Movie]
            }

            type Genre {
                name: String
            }
            
            type Rating {
                name: String
            }
            
            enum Language {
                ENGLISH
            }
            
            extend input MovieFilter {
                rating: Rating
            }
            
            extend type Movie {
                rating: Rating
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true
            )
        ).generate() as CodeGenResult

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(4) // IMovie, IMoviePage, IGenre, IRating
        assertThat(dataTypes).hasSize(5) // Movie, MoviePage, Genre, Rating, MovieFilter

        val iMovie = interfaces[0]
        assertThat(iMovie.typeSpec.name).isEqualTo("IMovie")
        assertThat(iMovie.typeSpec.methodSpecs).extracting("name").containsExactly("getId", "getTitle", "getGenre", "getLanguage", "getTags", "getRating")
        assertThat(iMovie.typeSpec.methodSpecs[0].returnType).extracting("simpleName").containsExactly("String")
        assertThat(iMovie.typeSpec.methodSpecs[1].returnType).extracting("simpleName").containsExactly("String")
        assertThat(iMovie.typeSpec.methodSpecs[2].returnType).extracting("simpleName").containsExactly("IGenre")
        assertThat(iMovie.typeSpec.methodSpecs[3].returnType).extracting("simpleName").containsExactly("Language")
        var parameterizedTypeName = iMovie.typeSpec.methodSpecs[4].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("String")
        assertThat(iMovie.typeSpec.methodSpecs[5].returnType).extracting("simpleName").containsExactly("IRating")

        val iMoviePage = interfaces[1]
        assertThat(iMoviePage.typeSpec.name).isEqualTo("IMoviePage")
        assertThat(iMoviePage.typeSpec.methodSpecs).extracting("name").containsExactly("getItems")
        parameterizedTypeName = iMoviePage.typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        val wildcardTypeName = parameterizedTypeName.typeArguments[0] as WildcardTypeName
        assertThat(wildcardTypeName.upperBounds[0]).extracting("simpleName").containsExactly("IMovie")

        val iGenre = interfaces[2]
        assertThat(iGenre.typeSpec.name).isEqualTo("IGenre")
        assertThat(iGenre.typeSpec.methodSpecs).extracting("name").containsExactly("getName")

        val iRating = interfaces[3]
        assertThat(iRating.typeSpec.name).isEqualTo("IRating")
        assertThat(iRating.typeSpec.methodSpecs).extracting("name").containsExactly("getName")

        val movie = dataTypes[0]
        assertThat(movie.typeSpec.name).isEqualTo("Movie")
        assertThat(movie.typeSpec.superinterfaces).extracting("simpleName").containsExactly("IMovie")
        assertThat(movie.typeSpec.fieldSpecs).extracting("name").containsExactly("id", "title", "genre", "language", "tags", "rating")
        assertThat(movie.typeSpec.fieldSpecs[0].type).extracting("simpleName").containsExactly("String")
        assertThat(movie.typeSpec.fieldSpecs[1].type).extracting("simpleName").containsExactly("String")
        assertThat(movie.typeSpec.fieldSpecs[2].type).extracting("simpleName").containsExactly("IGenre")
        assertThat(movie.typeSpec.fieldSpecs[3].type).extracting("simpleName").containsExactly("Language")
        parameterizedTypeName = movie.typeSpec.fieldSpecs[4].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("String")
        assertThat(movie.typeSpec.fieldSpecs[5].type).extracting("simpleName").containsExactly("IRating")

        val moviePage = dataTypes[1]
        assertThat(moviePage.typeSpec.name).isEqualTo("MoviePage")
        assertThat(moviePage.typeSpec.superinterfaces).extracting("simpleName").containsExactly("IMoviePage")
        assertThat(moviePage.typeSpec.fieldSpecs).extracting("name").containsExactly("items")
        parameterizedTypeName = moviePage.typeSpec.fieldSpecs[0].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("IMovie")

        val genre = dataTypes[2]
        assertThat(genre.typeSpec.name).isEqualTo("Genre")
        assertThat(genre.typeSpec.superinterfaces).extracting("simpleName").containsExactly("IGenre")
        assertThat(genre.typeSpec.fieldSpecs).extracting("name").containsExactly("name")

        val rating = dataTypes[3]
        assertThat(rating.typeSpec.name).isEqualTo("Rating")
        assertThat(rating.typeSpec.superinterfaces).extracting("simpleName").containsExactly("IRating")
        assertThat(rating.typeSpec.fieldSpecs).extracting("name").containsExactly("name")

        val movieFilter = dataTypes[4]
        assertThat(movieFilter.typeSpec.name).isEqualTo("MovieFilter")
        assertThat(movieFilter.typeSpec.superinterfaces.size).isEqualTo(0)
        assertThat(movieFilter.typeSpec.fieldSpecs).extracting("name").containsExactly("title", "genre", "language", "tags", "rating")
        assertThat(movieFilter.typeSpec.fieldSpecs[0].type).extracting("simpleName").containsExactly("String")
        assertThat(movieFilter.typeSpec.fieldSpecs[1].type).extracting("simpleName").containsExactly("Genre")
        assertThat(movieFilter.typeSpec.fieldSpecs[2].type).extracting("simpleName").containsExactly("Language")
        parameterizedTypeName = movieFilter.typeSpec.fieldSpecs[3].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("String")
        assertThat(movieFilter.typeSpec.fieldSpecs[4].type).extracting("simpleName").containsExactly("Rating")

        assertCompilesJava(dataTypes.plus(interfaces).plus(result.javaEnumTypes))
    }

    @Test
    fun generateInterfacesSupportingUnionTypes() {
        val schema = """
            type Query {
                search(text: String!): SearchResultPage
            }

            interface Character {
                id: ID!
                name: String!
            }
            
            type Human implements Character {
                id: ID!
                name: String!
                totalCredits: Int
            }

            type Droid implements Character {
                id: ID!
                name: String!
                primaryFunction: String
            }

            union SearchResult = Human | Droid
            
            type SearchResultPage {
                items: [SearchResult]
            }
            
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true
            )
        ).generate() as CodeGenResult

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(5) // IHuman, IDroid, ISearchResultPage, SearchResult, Character
        assertThat(dataTypes).hasSize(3) // Human, Droid, SearchResultPage

        assertThat(interfaces[0].typeSpec.name).isEqualTo("IHuman")
        assertThat(interfaces[1].typeSpec.name).isEqualTo("IDroid")

        val iSearchResultPage = interfaces[2]
        assertThat(iSearchResultPage.typeSpec.name).isEqualTo("ISearchResultPage")
        assertThat(iSearchResultPage.typeSpec.methodSpecs).extracting("name").containsExactly("getItems")
        var parameterizedTypeName = iSearchResultPage.typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("SearchResult")

        assertThat(interfaces[3].typeSpec.name).isEqualTo("SearchResult")
        assertThat(interfaces[4].typeSpec.name).isEqualTo("Character")

        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Human")
        assertThat(dataTypes[0].typeSpec.superinterfaces).extracting("simpleName").containsExactly("SearchResult", "IHuman", "com.netflix.graphql.dgs.codegen.tests.generated.types.Character")
        assertThat(dataTypes[1].typeSpec.name).isEqualTo("Droid")
        assertThat(dataTypes[1].typeSpec.superinterfaces).extracting("simpleName").containsExactly("SearchResult", "IDroid", "com.netflix.graphql.dgs.codegen.tests.generated.types.Character")

        val searchResultPage = dataTypes[2]
        assertThat(searchResultPage.typeSpec.name).isEqualTo("SearchResultPage")
        assertThat(searchResultPage.typeSpec.superinterfaces).extracting("simpleName").containsExactly("ISearchResultPage")
        assertThat(searchResultPage.typeSpec.fieldSpecs).extracting("name").containsExactly("items")
        parameterizedTypeName = searchResultPage.typeSpec.fieldSpecs[0].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("SearchResult")

        assertCompilesJava(dataTypes.plus(interfaces).plus(result.javaEnumTypes))
    }

    private fun compileAndGetConstructor(dataTypes: List<JavaFile>, type: String): ClassConstructor {
        val packageNameAsUnixPath = basePackageName.replace(".", "/")
        val compilation = assertCompilesJava(dataTypes)
        val temporaryFilesystem = Jimfs.newFileSystem(Configuration.unix())
        val classpath = compilation.generatedFiles()
            .filter { it.kind == JavaFileObject.Kind.CLASS }
            .map {
                val destFile = temporaryFilesystem.getPath(it.toUri().path)
                Files.createDirectories(destFile.parent)
                it.openInputStream().use { input ->
                    Files.newOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                destFile.parent
            }
            .toSet()
            .map { URL(it.toUri().toString().replace("$packageNameAsUnixPath.*".toRegex(), "")) }
            .toTypedArray()

        val clazz = URLClassLoader(classpath).loadClass("$basePackageName.types.$type")
        return ClassConstructor(clazz)
    }

    private val CodeGenResult.javaFiles: Collection<JavaFile>
        get() = javaDataTypes + javaInterfaces + javaEnumTypes + javaDataFetchers + javaQueryTypes + clientProjections + javaConstants
}
