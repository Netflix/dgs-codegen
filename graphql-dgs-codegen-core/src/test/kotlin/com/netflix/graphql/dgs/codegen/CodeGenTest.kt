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

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import javax.tools.DiagnosticCollector
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

@ExperimentalStdlibApi
internal class CodeGenTest {

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
        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
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


        compileGeneratedSources(dataTypes)
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


        compileGeneratedSources(dataTypes)
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
        compileGeneratedSources(dataTypes)
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
        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
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

        //Check data class
        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Employee")
        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(3)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("firstname", "lastname", "company")

        //Check interface
        assertThat(interfaces.size).isEqualTo(1)
        assertThat(interfaces[0].typeSpec.name).isEqualTo("Person")
        assertThat(interfaces[0].typeSpec.methodSpecs.size).isEqualTo(4)
        assertThat(interfaces[0].typeSpec.methodSpecs).extracting("name").contains("getFirstname", "setFirstname", "getLastname", "setLastname")

        compileGeneratedSources(dataTypes + interfaces)
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

        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(codeGenResult.enumTypes)
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
        compileGeneratedSources(dataFetchers + dataTypes)
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
        compileGeneratedSources(dataTypes)
    }

    @Test
    fun generateDataClassesWithCommonInterfaceTypes() {
        val schema = """
            type Query {
                now: Date
                upload: PresignedUrlResponse
            }
            
            type UploadImageResponse implements PresignedUrlResponse {
                url: String
                headers: [Header]
                method: String
                uploadId: ID
            }
        """.trimIndent()


        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, typeMapping = mapOf()))
                .generate() as CodeGenResult

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("UploadImageResponse")
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)

        assertThat(dataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(4)
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").contains("url", "headers", "method", "uploadId")
        compileGeneratedSources(dataTypes)
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

        compileGeneratedSources(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForInputTypes() {

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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedInputString = """
            return "{" + "genre:\"" + genre + "\"," +"rating:" + rating + "," +"views:" + views + "," +"stars:" + stars + "" +"}";
        """.trimIndent()
        val generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        compileGeneratedSources(dataTypes)
    }

    @Test
    fun generateToInputStringMethodForListOfString() {

        val schema = """
            type Query {
                movies(filter: MovieFilter)
            }
            
            input MovieFilter {
                genre: [String]!
                actors: [String]!
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate() as CodeGenResult
        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")

        var expectedInputString = """
            return "{" + "genre:" + serializeListOfStrings(genre) + "," +"actors:" + serializeListOfStrings(actors) + "" +"}";
        """.trimIndent()
        var generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("serializeListOfStrings")
        expectedInputString = """
                StringBuilder builder = new java.lang.StringBuilder();
                builder.append("[");
            
                String result = genre.stream()
                        .collect(java.util.stream.Collectors.joining("\", \"", "\"", "\""));
                builder.append(result);
                builder.append("]");
                return  builder.toString();
        """.trimIndent()
        generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "serializeListOfStrings" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        compileGeneratedSources(dataTypes)
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

        var expectedInputString = """
            return "{" + "genre:" + genre + "" +"}";
        """.trimIndent()
        var generatedInputString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedInputString).isEqualTo(generatedInputString)
        compileGeneratedSources(dataTypes)
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
        compileGeneratedSources(dataTypes)
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
        compileGeneratedSources(dataTypes)
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

    private fun compileGeneratedSources(dataTypes: List<JavaFile>) {
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        val diagnosticCollector = DiagnosticCollector<JavaFileObject>()
        val manager = compiler.getStandardFileManager(
                diagnosticCollector, null, null )

        val generatedFiles = dataTypes.map { it.toJavaFileObject() }

        Files.createDirectories(Paths.get("compiled-sources"))
        val compilationResult = compiler.getTask(null, manager, diagnosticCollector, listOf("-d", "compiled-sources"), null, generatedFiles).call()

        if (!compilationResult) {
            fail<Boolean>("Error compiling generated souces: ${diagnosticCollector.diagnostics}")
        }
    }
}