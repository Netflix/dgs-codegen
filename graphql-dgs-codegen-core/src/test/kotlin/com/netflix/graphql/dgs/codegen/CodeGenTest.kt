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
import com.netflix.graphql.dgs.codegen.generators.java.disableJsonTypeInfoAnnotation
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CodeGenTest {

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


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname")
        assertThat(typeSpec.methodSpecs).flatExtracting("parameters").extracting("name").contains("firstname", "lastname")
        dataTypes[0].writeTo(System.out)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("int")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("boolean")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("double")
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        val toString = assertThat(dataTypes[0].typeSpec.methodSpecs).filteredOn("name", "toString")
        toString.extracting("code").allMatch {it.toString().contains("return \"Person{")}

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting ("name").contains("equals")

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting ("name").contains("equals")


        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting ("name").contains("newBuilder")
        val builderType = dataTypes[0].typeSpec.typeSpecs[0]
        assertThat(builderType.name).isEqualTo("Builder")
        assertThat(builderType.methodSpecs).extracting("name").contains("firstname", "lastname", "build")
        assertThat(builderType.methodSpecs).filteredOn("name", "firstname").extracting("returnType.simpleName").contains("com.netflix.graphql.dgs.codegen.tests.generated.types.Person.Builder")
        assertThat(builderType.methodSpecs).filteredOn("name", "build").extracting("returnType.simpleName").contains("Person")
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting ("name").contains("hashCode")

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = "com.mypackage")).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo("com.mypackage.types")

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("name", "email")
        val type = assertThat(dataTypes[0].typeSpec.fieldSpecs).filteredOn("name", "email").extracting("type")
        type.extracting("rawType.canonicalName").contains("java.util.List")
        type.flatExtracting("typeArguments").extracting("canonicalName").contains("java.lang.String")

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("name", "email")
        val type = assertThat(dataTypes[0].typeSpec.fieldSpecs).filteredOn("name", "email").extracting("type")
        type.extracting("rawType.canonicalName").contains("java.util.List")
        type.flatExtracting("typeArguments").extracting("canonicalName").contains("java.lang.String")

        assertCompiles(dataTypes)
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

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        //Check data class
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
               |  void setFirstname(String firstname);
               |
               |  String getLastname();
               |
               |  void setLastname(String lastname);
               |}
               |""".trimMargin())

        assertCompiles(dataTypes + interfaces)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(3)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname", "friends")

        //Check type of friends field
        val parameterizedType = ParameterizedTypeName.get(ClassName.get(List::class.java), ClassName.get(typesPackageName, "Person"))
        assertThat(dataTypes[0].typeSpec.fieldSpecs)
                .withFailMessage("Incorrect type for friends field. List<Person> expected.")
                .filteredOn { it.name == "friends" }
                .extracting("type", ParameterizedTypeName::class.java)
                .contains(parameterizedType)

        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes).extracting("typeSpec.name").contains("Car", "Engine", "Performance")
        assertThat(dataTypes)
                .filteredOn("typeSpec.name", "Engine")
                .extracting("typeSpec")
                .flatExtracting("fieldSpecs")
                .filteredOn("name", "performance")
                .extracting("type.simpleName")
                .containsExactly("Performance")

        assertCompiles(dataTypes)
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        //Check generated enum type
        assertThat(codeGenResult.enumTypes.size).isEqualTo(1)
        assertThat(codeGenResult.enumTypes[0].typeSpec.name).isEqualTo("EmployeeTypes")
        assertThat(codeGenResult.enumTypes[0].typeSpec.enumConstants.size).isEqualTo(3)
        assertThat(codeGenResult.enumTypes[0].typeSpec.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")

        assertCompiles(codeGenResult.enumTypes)
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val dataFetchers = codeGenResult.dataFetchers
        val dataTypes = codeGenResult.dataTypes

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].typeSpec.name).isEqualTo("PeopleDatafetcher")
        assertThat(dataFetchers[0].packageName).isEqualTo(dataFetcherPackageName)
        assertCompiles(dataFetchers + dataTypes)
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


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, typeMapping = mapOf(Pair("Date", "java.time.LocalDateTime")))).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(3)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname", "birthDate")
        dataTypes[0].writeTo(System.out)
        assertCompiles(dataTypes)
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


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("genre")

        assertCompiles(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: String
                rating: Int = 3
                viewed: Boolean = true
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + (genre != null?"\"":"") + genre + (genre != null?"\"":"") + "," +"rating:" + rating + "," +"viewed:" + viewed + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForNonNullableInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: String!
                rating: Int!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + (genre != null?"\"":"") + genre + (genre != null?"\"":"") + "," +"rating:" + rating + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + (genre != null?"\"":"") + genre + (genre != null?"\"":"") + "," +"rating:" + rating + "," +"average:" + average + "," +"viewed:" + viewed + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:" + (genre != null?"\"":"") + genre + (genre != null?"\"":"") + "," +"rating:" + rating + "," +"average:" + average + "," +"viewed:" + viewed + "," +"identifier:" + (identifier != null?"\"":"") + identifier + (identifier != null?"\"":"") + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForListOfString() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: [String]
                actors: [String]!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        var expectedInputString = """
            return "{" + "genre:" + serializeListOfString(genre) + "," +"actors:" + serializeListOfString(actors) + "" +"}";
        """.trimIndent()
        var generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("serializeListOfString")
        expectedInputString = """
            if (inputList == null) {
                    return null;
                }
                StringBuilder builder = new java.lang.StringBuilder();
                builder.append("[");
            
                if (! inputList.isEmpty()) {
                    String result = inputList.stream()
                            .map( iter -> iter.toString() )
                            .collect(java.util.stream.Collectors.joining("\", \"", "\"", "\""));
                    builder.append(result);
                }
                builder.append("]");
                return  builder.toString();
        """.trimIndent()
        generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "serializeListOfString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForListOfIntegers() {
        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: [Integer]
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "genre:" + genre + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "localDateTime:" + (localDateTime != null?"\"":"") + localDateTime + (localDateTime != null?"\"":"") + "," +"localDate:" + (localDate != null?"\"":"") + localDate + (localDate != null?"\"":"") + "," +"localTime:" + (localTime != null?"\"":"") + localTime + (localTime != null?"\"":"") + "," +"dateTime:" + (dateTime != null?"\"":"") + dateTime + (dateTime != null?"\"":"") + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName,
                typeMapping = mapOf(Pair("LocalTime", "java.time.LocalDateTime"), Pair("LocalDate", "java.lang.Integer")))).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "time:" + (time != null?"\"":"") + time + (time != null?"\"":"") + "," +"date:" + date + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        val expectedInputString = """
            return "{" + "names:" + serializeListOfString(names) + "," +"localDateTime:" + serializeListOfLocalDateTime(localDateTime) + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(generatedInputString).isEqualTo(expectedInputString)
        assertCompiles(dataTypes)
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


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("MovieFilter")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("genre", "releaseYear")
        assertCompiles(dataTypes)
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
        assertCompiles(dataTypes)
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


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val type = result.constants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "People")
        assertThat(type.typeSpecs[1].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "Firstname", "Lastname")
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


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val type = result.constants[0].typeSpec
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


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val type = result.constants[0].typeSpec
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


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val type = result.constants[0].typeSpec
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


        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        val type = result.constants[0].typeSpec
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

        val (dataTypes, interfaces) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Movie")
        assertThat(dataTypes[1].typeSpec.name).isEqualTo("Actor")
        assertThat(interfaces[0].typeSpec.name).isEqualTo("SearchResult")
        val typeSpec = dataTypes[0]

        assertThat(typeSpec.typeSpec.superinterfaces[0]).isEqualTo(ClassName.get("com.netflix.graphql.dgs.codegen.tests.generated.types", "SearchResult"))
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").containsExactly("name")
    }

    @Test
    fun generateObjectTypeInterface() {
        val schema = """
            type Query {
                person: Person
                people: [Person]
            }

            type Person {
                firstname: String
                lastname: String
                address: Address
            }
            
            type Address {
                street: String
            }
        """.trimIndent()

        val result = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateInterfaces = true)).generate() as CodeGenResult

        val dataFetchers = result.dataFetchers
        assertThat(dataFetchers.size).isEqualTo(2)
        assertThat(dataFetchers[0].typeSpec.name).isEqualTo("PersonDatafetcher")
        assertThat(dataFetchers[0].typeSpec.methodSpecs).extracting("name").containsExactly("getPerson")
        assertThat(dataFetchers[0].typeSpec.methodSpecs[0].returnType).extracting("simpleName").containsExactly("IPerson")
        assertThat(dataFetchers[1].typeSpec.name).isEqualTo("PeopleDatafetcher")
        assertThat(dataFetchers[1].typeSpec.methodSpecs).extracting("name").containsExactly("getPeople")
        val parameterizedTypeName = dataFetchers[1].typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").containsExactly("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").containsExactly("IPerson")

        val dataTypes = result.dataTypes
        assertThat(dataTypes.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.superinterfaces).extracting("simpleName").containsExactly("IPerson")
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").containsExactly("firstname", "lastname", "address")
        assertThat(dataTypes[0].typeSpec.fieldSpecs[2].type).extracting("simpleName").containsExactly("IAddress")
        assertThat(dataTypes[1].typeSpec.name).isEqualTo("Address")
        assertThat(dataTypes[1].typeSpec.superinterfaces).extracting("simpleName").containsExactly("IAddress")
        assertThat(dataTypes[1].typeSpec.fieldSpecs).extracting("name").containsExactly("street")

        val interfaces = result.interfaces
        assertThat(interfaces.size).isEqualTo(2)
        assertThat(interfaces[0].typeSpec.name).isEqualTo("IPerson")
        assertThat(interfaces[0].typeSpec.methodSpecs).extracting("name").containsExactly("getFirstname", "getLastname", "getAddress")
        assertThat(interfaces[1].typeSpec.name).isEqualTo("IAddress")
        assertThat(interfaces[1].typeSpec.methodSpecs).extracting("name").containsExactly("getStreet")

        assertCompiles(dataTypes.plus(interfaces))
    }
}
