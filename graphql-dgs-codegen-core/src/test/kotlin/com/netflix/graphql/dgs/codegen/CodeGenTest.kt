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

import com.netflix.graphql.dgs.codegen.generators.java.disableJsonTypeInfoAnnotation
import com.squareup.javapoet.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.junit.jupiter.params.provider.Arguments.arguments
import java.io.Serializable
import java.util.stream.Stream

class CodeGenTest {

    @Test
    fun `When the schema fails to parse, is able to print the error message along with the schema`() {
        val schema = """
            type Query {
                people: [Person]
            }
            type Person {
                firstname: String
                lastname: String
            }
            type Mutation {
        """.trimIndent()

        Assertions.assertThatThrownBy {
            CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
        }.isInstanceOf(CodeGenSchemaParsingException::class.java)
            .hasMessageContainingAll(
                "Invalid Syntax : offending token '<EOF>' at line 8 column 16",
                """
                |Schema Section:
                |>>>
                |    firstname: String
                |    lastname: String
                |}
                |type Mutation {
                |
                """.trimMargin(),
                """Full Schema:
                |type Query {
                |    people: [Person]
                |}
                |type Person {
                |    firstname: String
                |    lastname: String
                |}
                |type Mutation {
                """.trimMargin()
            )
    }

    @Test
    fun `When the schema is empty, there is no parsing error`() {
        val schema = """"""

        CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
    }

    @Test
    fun `When the schema contains just whitespace, there is no parsing error`() {
        val schema = """     """

        CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
    }

    @Test
    fun `When the schema is just an opening bracket, a parsing error is thrown`() {
        val schema = """{""".trimIndent()

        Assertions.assertThatThrownBy {
            CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
        }.isInstanceOf(CodeGenSchemaParsingException::class.java)
    }

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("int")
        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("boolean")
        assertThat(typeSpec.fieldSpecs[2].type.toString()).isEqualTo("double")
    }

    @Test
    fun generateDataClassWithBooleanPrimitiveCreatesIsGetter() {
        val schema = """
            type MyType {
                truth: Boolean!
                boxedTruth: Boolean
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateIsGetterForPrimitiveBooleanFields = true
            )
        ).generate()
        val typeSpec = dataTypes[0].typeSpec
        assertThat(typeSpec.fieldSpecs[0].type.toString()).isEqualTo("boolean")
        assertThat(typeSpec.methodSpecs[0].returnType.toString()).isEqualTo("boolean")
        assertThat(typeSpec.methodSpecs[0].name.toString()).isEqualTo("isTruth")
        assertThat(typeSpec.methodSpecs[1].name.toString()).isEqualTo("setTruth")

        assertThat(typeSpec.fieldSpecs[1].type.toString()).isEqualTo("java.lang.Boolean")
        assertThat(typeSpec.methodSpecs[2].returnType.toString()).isEqualTo("java.lang.Boolean")
        assertThat(typeSpec.methodSpecs[2].name.toString()).isEqualTo("getBoxedTruth")
        assertThat(typeSpec.methodSpecs[3].name.toString()).isEqualTo("setBoxedTruth")
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateBoxedTypes = true)).generate()
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        val toString = assertThat(dataTypes[0].typeSpec.methodSpecs).filteredOn("name", "toString")
        toString.extracting("code").allMatch { "return \"Person{" in it.toString() }

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = "com.mypackage"
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        // Check data class
        assertThat(employee.name).isEqualTo("Employee")
        assertThat(employee.fieldSpecs.size).isEqualTo(3)
        assertThat(employee.fieldSpecs).extracting("name").contains("firstname", "lastname", "company")

        val annotation = employee.annotations.single()
        assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        val person = interfaces[0]
        assertThat(person.toString()).isEqualTo(
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
               |
            """.trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithBooleanPrimitiveCreatesIsGetter() {
        val schema = """
            type Query {
                featureToggles: [FeatureToggle]
            }
            
            interface FeatureToggle {
                enabled: Boolean!
                boxedEnabled: Boolean
            }
            
            type AdminFeatureToggle implements FeatureToggle {
                enabled: Boolean!
                boxedEnabled: Boolean
            }
        """.trimIndent()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateIsGetterForPrimitiveBooleanFields = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        // Check data class
        assertThat(employee.name).isEqualTo("AdminFeatureToggle")
        assertThat(employee.fieldSpecs.size).isEqualTo(2)
        assertThat(employee.fieldSpecs).extracting("name").contains("enabled", "boxedEnabled")

        val annotation = employee.annotations.single()
        assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        val person = interfaces[0]
        assertThat(person.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import com.fasterxml.jackson.annotation.JsonSubTypes;
               |import com.fasterxml.jackson.annotation.JsonTypeInfo;
               |import java.lang.Boolean;
               |
               |@JsonTypeInfo(
               |    use = JsonTypeInfo.Id.NAME,
               |    include = JsonTypeInfo.As.PROPERTY,
               |    property = "__typename"
               |)
               |@JsonSubTypes(@JsonSubTypes.Type(value = AdminFeatureToggle.class, name = "AdminFeatureToggle"))
               |public interface FeatureToggle {
               |  boolean isEnabled();
               |
               |  void setEnabled(boolean enabled);
               |
               |  Boolean getBoxedEnabled();
               |
               |  void setBoxedEnabled(Boolean boxedEnabled);
               |}
               |
            """.trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithInterfaceFields() {
        // schema contains nullable, non-nullable and list types as interface fields  and fields that are
        // not interfaces
        val schema = """
            |interface Pet {
            |   id: ID!
            |   name: String
            |   address: [String!]!
            |   mother: Pet!
            |   father: Pet
            |   parents: [Pet]
            |}
            |
            |type Dog implements Pet {
            |    id: ID!
            |    name: String
            |    address: [String!]!
            |    mother: Dog!
            |    father: Dog
            |    parents: [Dog]
            |}
            | 
            |type Bird implements Pet {
            |   id: ID!
            |   name: String
            |   address: [String!]!
            |   mother: Bird!
            |   father: Bird
            |   parents: [Bird]
            |}
        """.trimMargin()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(interfaces[0].toString()).isEqualTo(
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
                |  void setId(String id);
                |
                |  String getName();
                |
                |  void setName(String name);
                |
                |  List<String> getAddress();
                |
                |  void setAddress(List<String> address);
                |}
            |
            """.trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceClassWithInterfaceFieldsOfDifferentType() {
        val schema = """
            |interface Pet {
            |   name: String
            |   diet: Diet
            |}
            |     
            |interface Diet {
            |   calories: String
            |}
            |    
            |type Vegetarian implements Diet {
            |    calories: String
            |    vegetables: [String]
            |}
            |    
            |type Dog implements Pet {
            |    name: String
            |    diet: Vegetarian
            |}
        """.trimMargin()

        val (dataTypes, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(interfaces[0].toString()).isEqualTo(
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
                |
                |  void setName(String name);
                |}
            |
            """.trimMargin()
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
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val employee = dataTypes.single().typeSpec
        // Check data class
        assertThat(employee.name).isEqualTo("Employee")
        assertThat(employee.fieldSpecs.size).isEqualTo(3)
        assertThat(employee.fieldSpecs).extracting("name").contains("firstname", "lastname", "company")

        val annotation = employee.annotations.single()
        assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        val person = interfaces[0]
        assertThat(person.toString()).isEqualTo(
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
               |
            """.trimMargin()
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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

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
    fun generateDataClassWithNoAllConstructor() {
        val schema = """
            type Query {
                cars: [Car]
            }
            
            type Car {
                make: String
                model: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                javaGenerateAllConstructor = false
            )
        ).generate()

        assertThat(dataTypes[0].typeSpec.methodSpecs).filteredOn { it.name.equals("<init>") && it.parameters.size > 0 }.hasSize(0)
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateDataClassWithAllConstructor() {
        val schema = """
            type Query {
                cars: [Car]
            }
            
            type Car {
                make: String
                model: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes[0].typeSpec.methodSpecs).filteredOn { it.name.equals("<init>") && it.parameters.size > 0 }.hasSize(1)
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
                packageName = basePackageName
            )
        ).generate()

        // Check generated enum type
        assertThat(codeGenResult.javaEnumTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.name).isEqualTo("EmployeeTypes")
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants.size).isEqualTo(3)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants).containsKeys("ENGINEER", "MANAGER", "DIRECTOR")

        assertCompilesJava(codeGenResult.javaEnumTypes)
    }

    @Test
    fun generateEnumWithReservedKeywords() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            enum EmployeeTypes {
                default
                root
                new
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        // Check generated enum type
        assertThat(codeGenResult.javaEnumTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.name).isEqualTo("EmployeeTypes")
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants.size).isEqualTo(3)
        assertThat(codeGenResult.javaEnumTypes[0].typeSpec.enumConstants).containsKeys("_default", "_root", "_new")

        assertCompilesJava(codeGenResult.javaEnumTypes)
    }

    @Test
    fun generateDataWithReservedKeywords() {
        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                package: Parcel
            }
            
            type Parcel {
                 name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertCompilesJava(codeGenResult)
    }

    @Nested
    inner class EnumAnnotationTest {
        @Test
        fun `generates annotations from directive`() {
            val schema = """
            enum EmployeeTypes {
                ENGINEER @deprecated(reason: "chatGPT does the engineering now")
                MANAGER
                DIRECTOR
            }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    addDeprecatedAnnotation = true
                )
            ).generate()

            val enum = codeGenResult.javaEnumTypes[0].toString()

            assertThat(enum).isEqualTo(
                """
            package com.netflix.graphql.dgs.codegen.tests.generated.types;
            
            import java.lang.Deprecated;
            
            public enum EmployeeTypes {
              /**
               * chatGPT does the engineering now
               */
              @Deprecated
              ENGINEER,
            
              MANAGER,
            
              DIRECTOR
            }
            
                """.trimIndent()
            )

            assertCompilesJava(codeGenResult.javaEnumTypes)
        }

        @Test
        fun `adds custom annotation when setting enabled`() {
            val schema = """
                enum SomeEnum {
                    ENUM_VALUE @annotate(name: "ValidName", type: "validator")
                }
            """.trimIndent()

            val codeGenResult = CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateCustomAnnotations = true
                )
            ).generate()

            val enum = codeGenResult.javaEnumTypes[0].toString()

            assertThat(enum).isEqualTo(
                """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |public enum SomeEnum {
                |  @ValidName
                |  ENUM_VALUE
                |}
                |
                """.trimMargin()
            )
        }
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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()
        val dataFetchers = codeGenResult.javaDataFetchers
        val dataTypes = codeGenResult.javaDataTypes

        assertThat(dataFetchers.size).isEqualTo(1)
        assertThat(dataFetchers[0].typeSpec.name).isEqualTo("PeopleDatafetcher")
        assertThat(dataFetchers[0].packageName).isEqualTo(dataFetcherPackageName)
        assertCompilesJava(dataFetchers + dataTypes)
    }

    class MappedTypesTestCases : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments> = Stream.of(
            arguments("java.time.LocalDateTime", "java.time.LocalDateTime"),
            arguments("String", "java.lang.String"),
            arguments("BigDecimal", "java.math.BigDecimal"),
            arguments("Map", "java.util.Map"),
            arguments("ArrayList", "java.util.ArrayList"),
            arguments("ArrayList<String>", "java.util.ArrayList<java.lang.String>"),
            arguments("java.util.Map", "java.util.Map"),
            arguments("java.lang.String", "java.lang.String"),
            arguments("Map<String, Object>", "java.util.Map<java.lang.String, java.lang.Object>"),
            arguments("ArrayList<? extends Number>", "java.util.ArrayList<? extends java.lang.Number>"),
            arguments("ArrayList<? super Integer>", "java.util.ArrayList<? super java.lang.Integer>"),
            arguments("Map<? extends Double, ? super Float>", "java.util.Map<? extends java.lang.Double, ? super java.lang.Float>"),
            arguments("ArrayList<LinkedList<Set<HashSet<String>>>>", "java.util.ArrayList<java.util.LinkedList<java.util.Set<java.util.HashSet<java.lang.String>>>>"),
            arguments("Map<Map<Byte, Short>, Map<Long, Boolean>>", "java.util.Map<java.util.Map<java.lang.Byte, java.lang.Short>, java.util.Map<java.lang.Long, java.lang.Boolean>>"),
            arguments("ArrayList<?>", "java.util.ArrayList<java.lang.Object>"),
            arguments("Map<?, ?>", "java.util.Map<java.lang.Object, java.lang.Object>")
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
            fun testValidCase(mappedTypeAsString: String, expected: String) {
                val (dataTypes) = CodeGen(
                    CodeGenConfig(
                        schemas = setOf(schema),
                        packageName = basePackageName,
                        typeMapping = mapOf("JSON" to mappedTypeAsString)
                    )
                ).generate()
                assertThat(dataTypes.size).isEqualTo(1)
                assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
                assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)
                assertThat(dataTypes[0].typeSpec.fieldSpecs).hasSize(3)
                assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name")
                    .contains("firstname", "data", "dataNotNullable")
                assertThat(dataTypes[0].typeSpec.fieldSpecs[1].type.toString()).isEqualTo(expected)
                assertThat(dataTypes[0].typeSpec.fieldSpecs[2].type.toString()).isEqualTo(expected)
                assertCompilesJava(dataTypes)
            }
        }

        @Nested
        inner class WrongCases {
            @ParameterizedTest
            @ValueSource(
                strings = [
                    "",
                    "java.util.Map<",
                    "java.util.Map>",
                    "java.util.Map><",
                    "java.util.Map<>",
                    "java.util.Map<String, java.util.ArrayList<>",
                    "java.util.Map<<String, java.util.ArrayList<String>>",
                    "java.util.Map<String, java.util.ArrayList<>>>",
                    "java.util.Map<? extends>",
                    "java.util.Map<? super>",
                    "java.util.Map<extends>",
                    "java.util.Map<super>",
                    "?"
                ]
            )
            fun testWrongCase(mappedTypeAsString: String) {
                assertThrows<IllegalArgumentException> {
                    CodeGen(
                        CodeGenConfig(
                            schemas = setOf(schema),
                            packageName = basePackageName,
                            typeMapping = mapOf("JSON" to mappedTypeAsString)
                        )
                    ).generate()
                }
            }
        }
    }

    @Test
    fun `Skip generating a data class when the type is mapped`() {
        val schema = """
            type Query {
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
                typeMapping = mapOf("Person" to "mypackage.Person")
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(0)
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

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf("Person" to "mypackage.Person")
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[0].type.toString()).isEqualTo("mypackage.Person")
    }

    @Test
    fun `Use mapped type name when the type implements not-mapped interface`() {
        val schema = """
            interface Pet {
              name: ID!
            }
            type Cat implements Pet {
              name: ID!
            }
            type Dog implements Pet {
              name: ID!
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.JAVA,
                typeMapping = mapOf(
                    "Cat" to "mypackage.Cat"
                )
            )
        ).generate()
        val interfaces = codeGenResult.javaInterfaces

        assertThat(interfaces.size).isEqualTo(1)
        assertThat(interfaces[0].toString()).isEqualTo(
            """
                |package com.netflix.graphql.dgs.codegen.tests.generated.types;
                |
                |import com.fasterxml.jackson.annotation.JsonSubTypes;
                |import com.fasterxml.jackson.annotation.JsonTypeInfo;
                |import java.lang.String;
                |import mypackage.Cat;
                |
                |@JsonTypeInfo(
                |    use = JsonTypeInfo.Id.NAME,
                |    include = JsonTypeInfo.As.PROPERTY,
                |    property = "__typename"
                |)
                |@JsonSubTypes({
                |    @JsonSubTypes.Type(value = Cat.class, name = "Cat"),
                |    @JsonSubTypes.Type(value = Dog.class, name = "Dog")
                |})
                |public interface Pet {
                |  String getName();
                |
                |  void setName(String name);
                |}
                |
            """.trimMargin()
        )
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

        val (dataTypes, javaInterfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "SomethingWithAName" to "mypackage.SomethingWithAName",
                    "Person" to "mypackage.Person"
                )
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[0].type.toString()).isEqualTo("mypackage.SomethingWithAName")

        assertThat(javaInterfaces).isEmpty()
    }

    @Test
    fun `Use mapped type name when the type is mapped for interface without custom implementation type`() {
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

        val (dataTypes, javaInterfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "SomethingWithAName" to "mypackage.SomethingWithAName"
                )
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(2)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[0].type.toString()).isEqualTo("mypackage.SomethingWithAName")
        assertThat((dataTypes[1].typeSpec.superinterfaces[0].toString())).contains("mypackage.SomethingWithAName")

        assertThat(javaInterfaces).isEmpty()
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

        val (dataTypes, javaInterfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "SearchResult" to "mypackage.SearchResult",
                    "Movie" to "mypackage.Movie",
                    "Actor" to "mypackage.Actor"
                )
            )
        ).generate()

        assertThat(dataTypes).isEmpty()
        assertThat(javaInterfaces).isEmpty()
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

        val (dataTypes, javaInterfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "Actor" to "mypackage.Actor"
                )
            )
        ).generate()

        assertThat(dataTypes).hasSize(1)
        assertThat(javaInterfaces).hasSize(1)
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Movie")
    }

    @Test
    fun `Use mapped type name for input type`() {
        val schema = """
            type Query {                
                search(input: SearchInput): String
            }
            
           input SearchInput {
            title: String
           }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "SearchInput" to "mypackage.SearchInput"
                )
            )
        ).generate()

        assertThat(dataTypes).hasSize(0)
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
                typeMapping = mapOf(
                    "State" to "mypackage.State"
                )
            )
        ).generate()

        assertThat(result.javaDataTypes).isEmpty()
        assertThat(result.javaEnumTypes).isEmpty()
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
                packageName = basePackageName
            )
        ).generate()

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

        val (dataTypes, _, enumTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
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
    fun generateInputWithDefaultValueForComplexType() {
        val schema = """
            enum Color {
                red
            }
            
            input ColorFilter {
                color: Color = red
            }
            
            input Car {
                color: Color = red
                make: String
            }
            
            input MyCar {
                car: Car = {color: red}
            }
        """.trimIndent()

        val (dataTypes, _, enumTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()

        assertCompilesJava(enumTypes + dataTypes)
    }

    @Test
    fun generateInputWithDefaultValueForArray() {
        val schema = """
            input SomeType {
                names: [String] = []
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
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

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
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

        val (dataTypes, _, enumTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName)).generate()
        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("SomeType")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("colors")
        assertThat(colorField.initializer.toString()).isEqualTo("""java.util.Arrays.asList($typesPackageName.Color.red)""")

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
                packageName = basePackageName
            )
        ).generate()

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
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateToStringMethodForSensitiveType() {
        val schema = """
            type Query {
                people: [Person]
            }

            type Person {
                firstname: String
                lastname: String
                password: String @sensitive(reason:"PII")
            }
            directive @sensitive on FIELD_DEFINITION
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "Person{" + "firstname='" + firstname + "'," +"lastname='" + lastname + "'," +"password='" + "*****" + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
        assertCompilesJava(dataTypes)
    }

    @Test
    fun generateToStringMethodForSensitiveInputType() {
        val schema = """
            type Query {
                people(filter: PersonFilter): [Person]
            }
            input PersonFilter {
                email: String @sensitive
            }
            directive @sensitive on INPUT_FIELD_DEFINITION
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes[0].typeSpec.methodSpecs).extracting("name").contains("toString")
        val expectedString = """
            return "PersonFilter{" + "email='" + "*****" + "'" +"}";
        """.trimIndent()
        val generatedString = dataTypes[0].typeSpec.methodSpecs.single { it.name == "toString" }.code.toString().trimIndent()
        assertThat(expectedString).isEqualTo(generatedString)
        assertCompilesJava(dataTypes)
    }

    @ParameterizedTest(name = "{index} => Snake Case? {0}; expected names {1}")
    @MethodSource("generateConstantsArguments")
    fun `Generates constants from Type names available via the DgsConstants class`(
        snakeCaseEnabled: Boolean,
        constantNames: List<String>
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
                snakeCaseConstantNames = snakeCaseEnabled
            )
        ).generate()
        val type = result.javaConstants[0].typeSpec
        assertThat(type.name).isEqualTo("DgsConstants")
        assertThat(type.typeSpecs).extracting("name").containsExactlyElementsOf(constantNames)
        assertThat(type.typeSpecs[0].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "People")
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "PERSON")
        assertThat(type.typeSpecs[0].fieldSpecs).extracting("name").containsExactly("TYPE_NAME", "People", "Friends")
    }

    @Test
    fun generateConstantsForQueryInputArguments() {
        val schema = """
            type Query {
                shows(titleFilter: String,moveFilter: MovieFilter): [Show]
            }
            
            type Show {
                name: String
            }
            
            input MovieFilter {
                title: String
                genre: Genre
                language: Language
                tags: [String]
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()
        val type = result.javaConstants[0].typeSpec
        assertThat(type.typeSpecs).extracting("name").containsExactly("QUERY", "SHOW", "MOVIEFILTER")
        assertThat(type.typeSpecs[0].typeSpecs).extracting("name").containsExactly("SHOWS_INPUT_ARGUMENT")
        assertThat(type.typeSpecs[0].typeSpecs[0].fieldSpecs).extracting("name")
            .containsExactly("TitleFilter", "MoveFilter")
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
        assertThat(result.javaDataTypes[0].typeSpec.name).isEqualTo("Movie")
        assertThat(result.javaDataTypes[1].typeSpec.name).isEqualTo("Actor")
        assertThat(result.javaDataTypes[2].typeSpec.name).isEqualTo("Rating")
        assertThat(result.javaInterfaces[0].typeSpec.name).isEqualTo("SearchResult")

        assertThat(result.javaInterfaces[0].toString()).isEqualTo(
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
            |
            """.trimMargin()
        )

        assertCompilesJava(result.javaDataTypes + result.javaInterfaces)
    }

    @Test
    fun generateOnlyRequiredDataTypesForQuery() {
        val schema = """
            type Query {
                shows(showFilter: ShowFilter): [Video]
                people(personFilter: PersonFilter): [Person]
            }
            
            union Video = Show | Movie
            
            type Movie {
                title: String
                duration: Int
                related: Related
            }
            
            type Related {
                 video: Video
            }
            
            type Show {
                title: String
                tags(from: Int, to: Int, sourceType: SourceType): [ShowTag]
                isLive(countryFilter: CountryFilter): Boolean
            }
            
            enum ShouldNotInclude { YES, NO }
            
            input NotUsed {
                field: String
            }
            
            input ShowFilter {
                title: String
                showType: ShowType
                similarTo: SimilarityInput
            }
            
            input SimilarityInput {
                tags: [String]
            }
            
            enum ShowType {
                MOVIE, SERIES
            }
            
            input CountryFilter {
                countriesToExclude: [String]
            }
                 
            enum SourceType { FOO, BAR }
           
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeQueries = setOf("shows"),
                generateDataTypes = false,
                writeToFiles = false
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowFilter", "SimilarityInput", "CountryFilter")
        assertThat(codeGenResult.javaEnumTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowType", "SourceType")
        assertThat(codeGenResult.clientProjections).isEmpty()

        assertCompilesJava(codeGenResult.javaDataTypes + codeGenResult.javaEnumTypes)
    }

    @Test
    fun generateAllDataTypesForAllQueriesWhenSetToTrue() {
        val schema = """
            type Query {
                shows(showFilter: ShowFilter): [Video]
                people(personFilter: PersonFilter): [Person]
            }
            
            union Video = Show | Movie
            
            type Movie {
                title: String
                duration: Int
                related: Related
            }
            
            type Related {
                 video: Video
            }
            
            type Show {
                title: String
                isLive(countryFilter: CountryFilter): Boolean
            }
            
            enum ShouldNotInclude { YES, NO }
            
            input NotUsed {
                field: String
            }
            
            input ShowFilter {
                title: String
                showType: ShowType
                similarTo: SimilarityInput
            }
            
            input SimilarityInput {
                tags: [String]
            }
            
            enum ShowType {
                MOVIE, SERIES
            }
            
            input CountryFilter {
                countriesToExclude: [String]
            }
                 
            enum SourceType { FOO, BAR }
           
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeQueries = setOf("shows"),
                generateDataTypes = true,
                writeToFiles = false
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes)
            .extracting("typeSpec").extracting("name").containsExactly("Movie", "Related", "Show", "Person", "NotUsed", "ShowFilter", "SimilarityInput", "CountryFilter")
        assertThat(codeGenResult.javaEnumTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShouldNotInclude", "ShowType", "SourceType")
        assertThat(codeGenResult.clientProjections).isEmpty()

        assertCompilesJava(codeGenResult.javaDataTypes + codeGenResult.javaEnumTypes + codeGenResult.javaInterfaces)
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
                packageName = basePackageName
            )
        ).generate()
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
                packageName = basePackageName
            )
        ).generate()
        assertThat(dataTypes[0].typeSpec.name).isEqualTo("Person")
        assertThat(dataTypes[0].typeSpec.fieldSpecs).extracting("name").containsExactly("name")
    }

    @Test
    fun skipCodegenOnInterfaceFields() {
        val schema = """
            interface Person {
                name: String
                email: String @skipcodegen
            }
        """.trimIndent()

        val (_, interfaces) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()
        assertThat(interfaces[0].typeSpec.name).isEqualTo("Person")
        assertThat(interfaces[0].typeSpec.methodSpecs).extracting("name").containsExactly("getName", "setName")
    }

    @Test
    fun generateWithCustomSubPackageName() {
        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, subPackageNameTypes = "mytypes")).generate()

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
            ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val talent = dataTypes.single().typeSpec
        // Check data class
        assertThat(talent.name).isEqualTo("Talent")
        assertThat(talent.fieldSpecs.size).isEqualTo(4)
        assertThat(talent.fieldSpecs)
            .extracting("name")
            .contains("firstname", "lastname", "company", "imdbProfile")

        val annotation = talent.annotations.single()
        assertThat(annotation).isEqualTo(disableJsonTypeInfoAnnotation())

        assertThat(interfaces).hasSize(2)

        val person = interfaces[0]
        assertThat(person.toString()).isEqualTo(
            """
               |package com.netflix.graphql.dgs.codegen.tests.generated.types;
               |
               |import java.lang.String;
               |
               |public interface Person {
               |  String getFirstname();
               |
               |  void setFirstname(String firstname);
               |
               |  String getLastname();
               |
               |  void setLastname(String lastname);
               |}
               |
            """.trimMargin()
        )

        val employee = interfaces[1]
        assertThat(employee.toString()).isEqualTo(
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
               |public interface Employee extends Person {
               |  String getFirstname();
               |
               |  void setFirstname(String firstname);
               |
               |  String getLastname();
               |
               |  void setLastname(String lastname);
               |
               |  String getCompany();
               |
               |  void setCompany(String company);
               |}
               |
            """.trimMargin()
        )

        assertThat(JavaFile.builder("$basePackageName.types", talent).build().toString()).isEqualTo(
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
                |
            """.trimMargin()
        )

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfacesWithoutSetters() {
        val schema = """
            type Query {
                people: [Person]
            }

            interface Person {
                firstname: String!
                lastname: String
            }

        """.trimIndent()

        val (dataTypes, interfaces) =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateInterfaceSetters = false
                )
            ).generate()

        assertThat(interfaces).hasSize(1)

        val person = interfaces[0]
        assertThat(person.toString()).isEqualTo(
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
               |
            """.trimMargin()
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
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaInterfaces).hasSize(1)
        assertThat(result.javaInterfaces[0].typeSpec.methodSpecs).hasSize(6)
        assertThat(result.javaInterfaces[0].typeSpec.methodSpecs).extracting("name")
            .containsExactly("getFirstname", "setFirstname", "getLastname", "setLastname", "getAge", "setAge")
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

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf())).generate()
        assertCompilesJava(codeGenResult.javaFiles)
    }

    @Test
    fun generateObjectTypeInterfaceShouldNotRedeclareFields() {
        val schema = """
            interface Fruit {
              seeds: [Seed]
            }

            type Apple implements Fruit {
              seeds: [Seed]
              truth: Boolean!
            }

            type Seed {
              shape: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true,
                generateIsGetterForPrimitiveBooleanFields = true
            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val iapple = interfaces[0]
        assertThat(iapple.typeSpec.name).isEqualTo("IApple")
        assertThat(iapple.typeSpec.fieldSpecs).isEmpty()

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generatedInterfacesShouldShouldHaveCorrectJsonTypeAnnotations() {
        val schema = """
            interface Fruit {
              seeds: [Seed]
            }

            type Apple implements Fruit {
              seeds: [Seed]
              truth: Boolean!
            }

            type Seed {
              shape: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true,
                generateIsGetterForPrimitiveBooleanFields = true
            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val iFruit = interfaces[2]
        assertThat(iFruit.typeSpec.annotations.size).isEqualTo(2)
        assertThat(iFruit.typeSpec.annotations[0].toString()).isEqualTo("@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property = \"__typename\")")
        assertThat(iFruit.typeSpec.annotations[1].toString()).isEqualTo("@com.fasterxml.jackson.annotation.JsonSubTypes(@com.fasterxml.jackson.annotation.JsonSubTypes.Type(value = com.netflix.graphql.dgs.codegen.tests.generated.types.Apple.class, name = \"Apple\"))")
        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateObjectTypeInterfaceWithPrimitiveBooleanShouldUseIsGetter() {
        val schema = """
            interface Truthy {
              truth: Boolean!
            }

            type Truth implements Truthy {
              truth: Boolean!
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaces = true,
                generateIsGetterForPrimitiveBooleanFields = true
            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val itruth = interfaces[0]
        assertThat(itruth.typeSpec.name).isEqualTo("ITruth")
        assertThat(itruth.typeSpec.fieldSpecs).isEmpty()

        val itruthy = interfaces[1]
        assertThat(itruthy.typeSpec.name).isEqualTo("Truthy")
        assertThat(itruthy.typeSpec.fieldSpecs).isEmpty()
        assertThat(itruthy.typeSpec.methodSpecs[0].name).isEqualTo("isTruth")

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfaceMethodsForInterfaceFields() {
        val schema = """
            interface Fruit {
              seeds: [Seed]
            }
            
            interface FruitCategory {
                color: String
                fruit: Fruit
            }

            type Apple implements Fruit {
              seeds: [Seed]
            }

            type Seed {
              shape: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaceMethodsForInterfaceFields = true
            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val fruit = interfaces[0]
        assertThat(fruit.typeSpec.name).isEqualTo("Fruit")
        assertThat(fruit.typeSpec.fieldSpecs).isEmpty()
        assertThat(fruit.typeSpec.methodSpecs.size).isEqualTo(2)
        assertThat(fruit.typeSpec.methodSpecs[0].name).isEqualTo("getSeeds")
        assertThat(fruit.typeSpec.methodSpecs[1].name).isEqualTo("setSeeds")

        val category = interfaces[1]
        assertThat(category.typeSpec.name).isEqualTo("FruitCategory")
        assertThat(category.typeSpec.fieldSpecs).isEmpty()
        assertThat(category.typeSpec.methodSpecs.size).isEqualTo(4)
        assertThat(category.typeSpec.methodSpecs[0].name).isEqualTo("getColor")
        assertThat(category.typeSpec.methodSpecs[1].name).isEqualTo("setColor")
        assertThat(category.typeSpec.methodSpecs[2].name).isEqualTo("getFruit")
        assertThat(category.typeSpec.methodSpecs[3].name).isEqualTo("setFruit")

        assertCompilesJava(dataTypes + interfaces)
    }

    @Test
    fun generateInterfacesWithMethodsForInterfaceFields() {
        val schema = """
            interface Fruit {
              seeds: [Seed]
            }
            
            interface FruitCategory {
                color: String
                fruit: Fruit
            }

            type Apple implements Fruit {
              seeds: [Seed]
            }

            type Seed {
              shape: String
            }
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateInterfaceMethodsForInterfaceFields = true,
                generateInterfaces = true
            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val iapple = interfaces[0]
        assertThat(iapple.typeSpec.name).isEqualTo("IApple")

        val iseed = interfaces[1]
        assertThat(iseed.typeSpec.name).isEqualTo("ISeed")
        assertThat(iseed.typeSpec.fieldSpecs).isEmpty()
        assertThat(iseed.typeSpec.methodSpecs.size).isEqualTo(1)
        assertThat(iseed.typeSpec.methodSpecs[0].name).isEqualTo("getShape")

        val fruit = interfaces[2]
        assertThat(fruit.typeSpec.name).isEqualTo("Fruit")
        assertThat(fruit.typeSpec.fieldSpecs).isEmpty()
        assertThat(fruit.typeSpec.methodSpecs.size).isEqualTo(2)
        assertThat(fruit.typeSpec.methodSpecs[0].name).isEqualTo("getSeeds")
        val parameterizedTypeName = fruit.typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        val wildcardTypeName = parameterizedTypeName.typeArguments[0] as WildcardTypeName
        assertThat(wildcardTypeName.upperBounds[0]).extracting("simpleName").isEqualTo("ISeed")
        assertThat(fruit.typeSpec.methodSpecs[1].name).isEqualTo("setSeeds")

        val category = interfaces[3]
        assertThat(category.typeSpec.name).isEqualTo("FruitCategory")
        assertThat(category.typeSpec.fieldSpecs).isEmpty()
        assertThat(category.typeSpec.methodSpecs.size).isEqualTo(4)
        assertThat(category.typeSpec.methodSpecs[0].name).isEqualTo("getColor")
        assertThat(category.typeSpec.methodSpecs[1].name).isEqualTo("setColor")
        assertThat(category.typeSpec.methodSpecs[2].name).isEqualTo("getFruit")
        assertThat(category.typeSpec.methodSpecs[3].name).isEqualTo("setFruit")

        assertCompilesJava(dataTypes + interfaces)
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
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        val iapple = interfaces[0]
        assertThat(iapple.typeSpec.name).isEqualTo("IApple")
        assertThat(iapple.typeSpec.superinterfaces.size).isEqualTo(1)
        assertThat((iapple.typeSpec.superinterfaces[0] as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(iapple.typeSpec.methodSpecs).isEmpty()

        val ibasket = interfaces[1]
        assertThat(ibasket.typeSpec.name).isEqualTo("IBasket")
        assertThat((ibasket.typeSpec.methodSpecs[0].returnType as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(ibasket.typeSpec.methodSpecs).extracting("name").containsExactly("getFruit")

        val fruit = interfaces[2]
        assertThat(fruit.typeSpec.name).isEqualTo("Fruit")
        assertThat(fruit.typeSpec.methodSpecs).extracting("name").containsExactly("getName", "setName")

        val apple = dataTypes[0]
        assertThat(apple.typeSpec.name).isEqualTo("Apple")
        assertThat(apple.typeSpec.superinterfaces.size).isEqualTo(2)
        assertThat(apple.typeSpec.methodSpecs).extracting("name").contains("getName")

        val basket = dataTypes[1]
        assertThat(basket.typeSpec.name).isEqualTo("Basket")
        assertThat((basket.typeSpec.methodSpecs[0].returnType as ClassName).simpleName()).isEqualTo("Fruit")
        assertThat(basket.typeSpec.methodSpecs).extracting("name").contains("getFruit")

        assertCompilesJava(dataTypes + interfaces)
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

        val (dataTypes, interfaces) =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName
                )
            ).generate()

        assertThat(dataTypes).hasSize(1)
        assertThat(interfaces).hasSize(2)

        assertThat(interfaces[1].typeSpec.superinterfaces).hasSize(1)
        assertThat((interfaces[1].typeSpec.superinterfaces[0] as ClassName).simpleName()).isEqualTo("Fruit")

        assertCompilesJava(dataTypes + interfaces)
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
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(3)

        val team = interfaces[0]
        assertThat(team.typeSpec.name).isEqualTo("ITeam")
        assertThat(team.typeSpec.methodSpecs).extracting("name").containsExactly("getName")
        assertThat(team.typeSpec.methodSpecs[0].returnType).extracting("simpleName").isEqualTo("String")

        val player = interfaces[1]
        assertThat(player.typeSpec.name).isEqualTo("IPlayer")
        assertThat(player.typeSpec.methodSpecs).extracting("name").containsExactly("getName")
        assertThat(player.typeSpec.methodSpecs[0].returnType).extracting("simpleName").isEqualTo("String")

        val standing = interfaces[2]
        assertThat(standing.typeSpec.name).isEqualTo("Standing")
        assertThat(standing.typeSpec.methodSpecs).extracting("name").containsExactly("getPosition", "setPosition", "getTeam", "setTeam")
        assertThat(standing.typeSpec.methodSpecs[0].returnType.toString()).contains("int")
        assertThat(standing.typeSpec.methodSpecs[2].returnType).extracting("simpleName").isEqualTo("ITeam")

        assertCompilesJava(dataTypes + interfaces)
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
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(4) // IMovie, IMoviePage, IGenre, IRating
        assertThat(dataTypes).hasSize(5) // Movie, MoviePage, Genre, Rating, MovieFilter

        val iMovie = interfaces[0]
        assertThat(iMovie.typeSpec.name).isEqualTo("IMovie")
        assertThat(iMovie.typeSpec.methodSpecs).extracting("name").containsExactly("getId", "getTitle", "getGenre", "getLanguage", "getTags", "getRating")
        assertThat(iMovie.typeSpec.methodSpecs[0].returnType).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[1].returnType).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[2].returnType).extracting("simpleName").isEqualTo("IGenre")
        assertThat(iMovie.typeSpec.methodSpecs[3].returnType).extracting("simpleName").isEqualTo("Language")
        var parameterizedTypeName = iMovie.typeSpec.methodSpecs[4].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[5].returnType).extracting("simpleName").isEqualTo("IRating")

        val iMoviePage = interfaces[1]
        assertThat(iMoviePage.typeSpec.name).isEqualTo("IMoviePage")
        assertThat(iMoviePage.typeSpec.methodSpecs).extracting("name").containsExactly("getItems")
        parameterizedTypeName = iMoviePage.typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        val wildcardTypeName = parameterizedTypeName.typeArguments[0] as WildcardTypeName
        assertThat(wildcardTypeName.upperBounds[0]).extracting("simpleName").isEqualTo("IMovie")

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
        assertThat(movie.typeSpec.fieldSpecs[0].type).extracting("simpleName").isEqualTo("String")
        assertThat(movie.typeSpec.fieldSpecs[1].type).extracting("simpleName").isEqualTo("String")
        assertThat(movie.typeSpec.fieldSpecs[2].type).extracting("simpleName").isEqualTo("IGenre")
        assertThat(movie.typeSpec.fieldSpecs[3].type).extracting("simpleName").isEqualTo("Language")
        parameterizedTypeName = movie.typeSpec.fieldSpecs[4].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("String")
        assertThat(movie.typeSpec.fieldSpecs[5].type).extracting("simpleName").isEqualTo("IRating")

        val moviePage = dataTypes[1]
        assertThat(moviePage.typeSpec.name).isEqualTo("MoviePage")
        assertThat(moviePage.typeSpec.superinterfaces).extracting("simpleName").containsExactly("IMoviePage")
        assertThat(moviePage.typeSpec.fieldSpecs).extracting("name").containsExactly("items")
        parameterizedTypeName = moviePage.typeSpec.fieldSpecs[0].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        val movieTypeName = parameterizedTypeName.typeArguments[0] as WildcardTypeName
        assertThat(movieTypeName.upperBounds[0]).extracting("simpleName").isEqualTo("IMovie")

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
        assertThat(movieFilter.typeSpec.fieldSpecs[0].type).extracting("simpleName").isEqualTo("String")
        assertThat(movieFilter.typeSpec.fieldSpecs[1].type).extracting("simpleName").isEqualTo("Genre")
        assertThat(movieFilter.typeSpec.fieldSpecs[2].type).extracting("simpleName").isEqualTo("Language")
        parameterizedTypeName = movieFilter.typeSpec.fieldSpecs[3].type as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("String")
        assertThat(movieFilter.typeSpec.fieldSpecs[4].type).extracting("simpleName").isEqualTo("Rating")

        assertCompilesJava(dataTypes + interfaces + result.javaEnumTypes)
    }

    @Test
    fun generateObjectTypeInterfaceWithoutDataTypes() {
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
            
            interface Genre {
                name: String
            }

            type ActionGenre implements Genre {
                name: String
                heroes: Int
            }
            
            type ComedyGenre implements Genre {
                name: String
                jokes: Int
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
                generateInterfaces = true,
                generateDataTypes = false

            )
        ).generate()

        val interfaces = result.javaInterfaces
        val dataTypes = result.javaDataTypes

        assertThat(interfaces).hasSize(6) // IMovie, IMoviePage, IGenre, IActionGenre, IComedyGenre, IRating
        assertThat(dataTypes).hasSize(0)

        val iMovie = interfaces[0]
        assertThat(iMovie.typeSpec.name).isEqualTo("IMovie")
        assertThat(iMovie.typeSpec.methodSpecs).extracting("name").containsExactly("getId", "getTitle", "getGenre", "getLanguage", "getTags", "getRating")
        assertThat(iMovie.typeSpec.methodSpecs[0].returnType).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[1].returnType).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[2].returnType).extracting("simpleName").isEqualTo("Genre")
        assertThat(iMovie.typeSpec.methodSpecs[3].returnType).extracting("simpleName").isEqualTo("Language")
        var parameterizedTypeName = iMovie.typeSpec.methodSpecs[4].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("String")
        assertThat(iMovie.typeSpec.methodSpecs[5].returnType).extracting("simpleName").isEqualTo("IRating")

        val iMoviePage = interfaces[1]
        assertThat(iMoviePage.typeSpec.name).isEqualTo("IMoviePage")
        assertThat(iMoviePage.typeSpec.methodSpecs).extracting("name").containsExactly("getItems")
        parameterizedTypeName = iMoviePage.typeSpec.methodSpecs[0].returnType as ParameterizedTypeName
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        val wildcardTypeName = parameterizedTypeName.typeArguments[0] as WildcardTypeName
        assertThat(wildcardTypeName.upperBounds[0]).extracting("simpleName").isEqualTo("IMovie")

        val iActionGenre = interfaces[2]
        assertThat(iActionGenre.typeSpec.name).isEqualTo("IActionGenre")
        assertThat(iActionGenre.typeSpec.methodSpecs).extracting("name").containsExactly("getHeroes")

        val iComedyGenre = interfaces[3]
        assertThat(iComedyGenre.typeSpec.name).isEqualTo("IComedyGenre")
        assertThat(iComedyGenre.typeSpec.methodSpecs).extracting("name").containsExactly("getJokes")

        val iRating = interfaces[4]
        assertThat(iRating.typeSpec.name).isEqualTo("IRating")
        assertThat(iRating.typeSpec.methodSpecs).extracting("name").containsExactly("getName")

        val iGenre = interfaces[5]
        assertThat(iGenre.typeSpec.name).isEqualTo("Genre")
        assertThat(iGenre.typeSpec.methodSpecs).extracting("name").containsExactly("getName", "setName")

        assertCompilesJava(dataTypes + interfaces + result.javaEnumTypes)
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
        ).generate()

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
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("SearchResult")

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
        assertThat(parameterizedTypeName.rawType).extracting("simpleName").isEqualTo("List")
        assertThat(parameterizedTypeName.typeArguments[0]).extracting("simpleName").isEqualTo("SearchResult")

        assertCompilesJava(dataTypes + interfaces + result.javaEnumTypes)
    }

    @Test
    fun generateClassJavaDoc() {
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaDataTypes[0].typeSpec.javadoc.toString()).isEqualTo(
            """Movies are fun to watch.
They also work well as examples in GraphQL.
            """.trimIndent()
        )

        assertThat(result.javaDataTypes[1].typeSpec.javadoc.toString()).isEqualTo(
            """Example filter for Movies.

It takes a title and such.
            """.trimIndent()
        )
    }

    @Test
    fun generateClassFieldsJavaDoc() {
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

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaDataTypes[0].typeSpec.fieldSpecs[0].javadoc.toString()).isEqualTo(
            """The original, non localized title with some specials characters : %!({[*$,.:;.
            """.trimIndent()
        )

        assertThat(result.javaDataTypes[1].typeSpec.fieldSpecs[0].javadoc.toString()).isEqualTo(
            """Starts-with filter
            """.trimIndent()
        )
    }

    @Test
    fun generateInterfaceJavaDoc() {
        val schema = """           
            ""${'"'}
            Anything with a title!
            ""${'"'}
            interface Titled {
                title: String
            }                                 
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaInterfaces[0].typeSpec.javadoc.toString()).isEqualTo(
            """Anything with a title!
            """.trimIndent()
        )
    }

    @Test
    fun generateInterfaceFieldsJavaDoc() {
        val schema = """                       
            interface Titled {
               ""${'"'}
                The original, non localized title.
                ""${'"'}
                title: String
            }                                 
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaInterfaces[0].typeSpec.methodSpecs[0].javadoc.toString()).isEqualTo(
            """The original, non localized title.
            """.trimIndent()
        )
    }

    @Test
    fun generateEnumJavaDoc() {
        val schema = """           
            ""${'"'}
            Some options
            ""${'"'}
            enum Color {
                red,white,blue
            }                                 
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(result.javaEnumTypes[0].typeSpec.javadoc.toString()).isEqualTo(
            """Some options
            """.trimIndent()
        )
    }

    @Test
    fun generateSerializableDataClass() {
        val schema = """
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                implementSerializable = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].packageName).isEqualTo(typesPackageName)
        assertThat(dataTypes[0].typeSpec.superinterfaces).contains(ClassName.get(Serializable::class.java))
    }

    private val CodeGenResult.javaFiles: Collection<JavaFile>
        get() = javaDataTypes + javaInterfaces + javaEnumTypes + javaDataFetchers + javaQueryTypes + clientProjections + javaConstants

    companion object {
        @JvmStatic
        fun generateConstantsArguments(): Stream<Arguments> {
            return Stream.of(
                arguments(
                    true,
                    listOf("QUERY", "PERSON", "PERSON_META_DATA", "V_PERSON_META_DATA", "V_1_PERSON_META_DATA", "URL_META_DATA")
                ),
                arguments(
                    false,
                    listOf("QUERY", "PERSON", "PERSONMETADATA", "VPERSONMETADATA", "V1PERSONMETADATA", "URLMETADATA")
                )
            )
        }
    }

    @Test
    fun generateSourceWithGeneratedAnnotation() {
        val schema = """
            type Query {
                employees(filter:EmployeeFilterInput) : [Person]
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
            enum EmployeeTypes {
                ENGINEER
                MANAGER
                DIRECTOR
            }
            
            input EmployeeFilterInput {
                rank: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.JAVA,
                addGeneratedAnnotation = true,
                generateClientApi = true
            )
        ).generate()

        val (generatedAnnotationFile, allSources) = codeGenResult.javaSources()
            .partition { it.typeSpec.name == "Generated" && it.typeSpec.kind == TypeSpec.Kind.ANNOTATION }

        allSources.assertJavaGeneratedAnnotation()
        assertThat(generatedAnnotationFile.single().toString())
            .contains("java.lang.annotation.Retention", "RetentionPolicy.CLASS")
        assertCompilesJava(codeGenResult)
    }

    @Test
    fun deprecateAnnotation() {
        val schema = """
            input Person @deprecated(reason: "This is going bye bye") {
                name: String @deprecated(reason: "This field is no longer available, replace with firstName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.javadoc.toString()).isEqualTo("This is going bye bye")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("Deprecated")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("java.lang.Deprecated")
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].javadoc.toString()).isEqualTo("@deprecated This field is no longer available. Replaced by firstName")
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("Deprecated")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("java.lang.Deprecated")
    }

    @Test
    fun annotateOnInput() {
        val schema = """
            input Person @annotate(name: "ValidPerson", type: "validator", inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: "ValidName", type: "validator")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(2)
        assertThat((person.annotations[0] as AnnotationSpec).members["maxLimit"]).isEqualTo(listOf(CodeBlock.of("\$L", 10)))
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "\"husband\", \"wife\"")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidName")
    }

    @Test
    fun annotateOnTypes() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: "ValidName", type: "validator")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(2)
        assertThat((person.annotations[0] as AnnotationSpec).members["maxLimit"]).isEqualTo(listOf(CodeBlock.of("\$L", 10)))
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "\"husband\", \"wife\"")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidName")
    }

    @Test
    fun annotateWithNullType() {
        val schema = """
            type Person @annotate(name: "com.validator.ValidPerson", type: null, inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: "ValidName", type: "validator")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(2)
        assertThat((person.annotations[0] as AnnotationSpec).members["maxLimit"]).isEqualTo(listOf(CodeBlock.of("\$L", 10)))
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "\"husband\", \"wife\"")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidName")
    }

    @Test
    fun annotateWithNullName() {
        val schema = """
            type Person @annotate(name: "com.validator.ValidPerson", type: null, inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: null, type: "validator")
            }
        """.trimIndent()

        assertThrows<IllegalArgumentException> {
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    generateCustomAnnotations = true
                )
            ).generate()
        }
    }

    @Test
    fun annotateOnTypesWithDefaultPackage() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(2)
        assertThat((person.annotations[0] as AnnotationSpec).members["maxLimit"]).isEqualTo(listOf(CodeBlock.of("\$L", 10)))
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "\"husband\", \"wife\"")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithDefaultPackageAndType() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {maxLimit: 10, types: ["husband", "wife"]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", type: "validator")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(2)
        assertThat((person.annotations[0] as AnnotationSpec).members["maxLimit"]).isEqualTo(listOf(CodeBlock.of("\$L", 10)))
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "\"husband\", \"wife\"")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithClassObjects() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {groups: "BasicValidation.class"}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeClassImports = mapOf("ValidPerson" to mapOf(Pair("BasicValidation", "com.test.validator.groups"))),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["groups"]).isEqualTo(listOf(CodeBlock.of("\$L", "com.test.validator.groups.BasicValidation.class")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithClassObjectsNoMapping() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {groups: "BasicValidation.class"}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["groups"]).isEqualTo(listOf(CodeBlock.of("\$S", "BasicValidation.class"))) // treat as string when no mapping is provided
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithListOfClassObjects() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {groups: ["BasicValidation.class","AdvanceValidation.class"]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeClassImports = mapOf(
                    "ValidPerson" to mapOf(
                        Pair("BasicValidation", "com.test.validator.groups"),
                        Pair("AdvanceValidation", "com.test.validator.groups")
                    )
                ),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["groups"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.test.validator.groups.BasicValidation.class, com.test.validator.groups.AdvanceValidation.class")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithMultipleListsOfClassObjects() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {groups: ["BasicValidation.class","AdvanceValidation.class"]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
                type: String @annotate(name: "ValidDateOfBirth", type: "dateOfBirth", inputs: {levels: ["PreliminaryValidation.class","SecondaryValidation.class"]}) 
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator"), Pair("dateOfBirth", "com.test.validator.dob")),
                includeClassImports = mapOf(
                    "ValidPerson" to mapOf(
                        Pair("BasicValidation", "com.test.validator.groups"),
                        Pair("AdvanceValidation", "com.test.validator.groups")
                    ),
                    "ValidDateOfBirth" to mapOf(
                        Pair("PreliminaryValidation", "com.test.validator.dob.levels"),
                        Pair("SecondaryValidation", "com.test.validator.dob.levels")
                    )
                ),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["groups"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.test.validator.groups.BasicValidation.class, com.test.validator.groups.AdvanceValidation.class")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(2)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")

        assertThat(fields[1].annotations).hasSize(1)
        assertThat(((fields[1].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidDateOfBirth")
        assertThat(((fields[1].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.dob.ValidDateOfBirth")
        assertThat((fields[1].annotations[0] as AnnotationSpec).members["levels"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.test.validator.dob.levels.PreliminaryValidation.class, com.test.validator.dob.levels.SecondaryValidation.class")))
    }

    @Test
    fun annotateOnTypesWithEnums() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {sexType: MALE}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("sexType" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["sexType"]).isEqualTo(listOf(CodeBlock.of("\$L", "com.enums.MALE")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithListOfEnums() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.enums.HUSBAND, com.enums.WIFE")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithEmptyType() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", type: "")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.enums.HUSBAND, com.enums.WIFE")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
    }

    @Test
    fun annotateOnTypesWithMultipleAnnotations() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidPerson")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.validator.ValidPerson")
        assertThat((person.annotations[0] as AnnotationSpec).members).hasSize(1)
        assertThat((person.annotations[0] as AnnotationSpec).members["types"]).isEqualTo(listOf(CodeBlock.of("{\$L}", "com.enums.HUSBAND, com.enums.WIFE")))
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(2)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
        assertThat(((fields[0].annotations[1] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("NullValue")
        assertThat(((fields[0].annotations[1] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.nullValidator.NullValue")
    }

    @Test
    fun annotateOnTypesWithoutName() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate
            }
        """.trimIndent()

        assertThrows<IllegalArgumentException> {
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    includeImports = mapOf(Pair("validator", "com.test.validator")),
                    includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                    generateCustomAnnotations = true
                )
            ).generate()
        }
    }

    @Test
    fun annotateOnTypesWithEmptyName() {
        val schema = """
            type Person @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "")
            }
        """.trimIndent()

        assertThrows<IllegalArgumentException> {
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    includeImports = mapOf(Pair("validator", "com.test.validator")),
                    includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                    generateCustomAnnotations = true
                )
            ).generate()
        }
    }

    @Test
    fun deprecateAnnotationWithNoReason() {
        val schema = """
            input Person @deprecated {
                name: String
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )

        ).generate()


        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("Deprecated")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("java.lang.Deprecated")
        assertThat(person.javadoc.toString()).isEmpty()
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(0)
    }

    @Test
    fun annotateOnTypesWithCustomAnnotationsDisabled() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = false,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(1)
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("Deprecated")
        assertThat(((person.annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("java.lang.Deprecated")
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(0)
    }

    @Test
    fun annotateOnTypesWithTargetsOnGet() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "get") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(2)
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        val methods = person.methodSpecs
        assertThat((methods[0] as MethodSpec).name).isEqualTo("getName")
        assertThat(methods[0].annotations).hasSize(1)
        assertThat(((methods[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
    }

    @Test
    fun annotateOnTypesWithTargetsOnField() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "field") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(2)
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(2)
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
        assertThat(((fields[0].annotations[0] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.anotherValidator.ValidName")
        assertThat(((fields[0].annotations[1] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("NullValue")
        assertThat(((fields[0].annotations[1] as AnnotationSpec).type as ClassName).canonicalName()).isEqualTo("com.test.nullValidator.NullValue")
        val methods = person.methodSpecs
        assertThat((methods[0] as MethodSpec).name).isEqualTo("getName")
        assertThat(methods[0].annotations).hasSize(0)
    }

    @Test
    fun annotateOnTypesWithTargetsOnSet() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "set") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(2)
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        val methods = person.methodSpecs
        assertThat((methods[0] as MethodSpec).name).isEqualTo("getName")
        assertThat(methods[0].annotations).hasSize(0)
        assertThat((methods[1] as MethodSpec).name).isEqualTo("setName")
        assertThat(methods[1].annotations).hasSize(1)
        assertThat(((methods[1].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
    }

    @Test
    fun annotateOnTypesWithTargetsOnSetParam() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "setparam") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(2)
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        val methods = person.methodSpecs
        assertThat((methods[0] as MethodSpec).name).isEqualTo("getName")
        assertThat(methods[0].annotations).hasSize(0)
        assertThat((methods[1] as MethodSpec).name).isEqualTo("setName")
        assertThat(methods[1].annotations).hasSize(0)
        val parameters = (methods[1] as MethodSpec).parameters
        assertThat(parameters).hasSize(1)
        assertThat(((parameters[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
    }

    @Test
    fun annotateOnTypesWithTargetsOnParam() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "param") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                includeImports = mapOf(Pair("validator", "com.test.validator")),
                includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                generateCustomAnnotations = true,
                addDeprecatedAnnotation = true
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        val person = dataTypes.single().typeSpec
        assertThat(person.name).isEqualTo("Person")
        assertThat(person.annotations).hasSize(2)
        val fields = person.fieldSpecs
        assertThat(fields).hasSize(1)
        assertThat(fields[0].annotations).hasSize(1)
        val methods = person.methodSpecs
        assertThat((methods[0] as MethodSpec).name).isEqualTo("getName")
        assertThat(methods[0].annotations).hasSize(0)
        assertThat((methods[1] as MethodSpec).name).isEqualTo("setName")
        assertThat(methods[1].annotations).hasSize(0)
        assertThat((methods[3] as MethodSpec).name).isEqualTo("<init>")
        assertThat(methods[3].annotations).hasSize(0)
        val parameters = (methods[3] as MethodSpec).parameters
        assertThat(parameters).hasSize(1)
        assertThat(((parameters[0].annotations[0] as AnnotationSpec).type as ClassName).simpleName()).isEqualTo("ValidName")
    }

    @Test
    fun annotateOnTypesWithTargetsOnInvalidTarget() {
        val schema = """
            type Person @deprecated(reason: "This is going bye bye") @annotate(name: "ValidPerson", type: "validator", inputs: {types: [HUSBAND, WIFE]}) {
                name: String @annotate(name: "com.test.anotherValidator.ValidName", target: "invalid") @annotate(name: "com.test.nullValidator.NullValue")
            }
        """.trimIndent()

        assertThrows<IllegalArgumentException> {
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName,
                    includeImports = mapOf(Pair("validator", "com.test.validator")),
                    includeEnumImports = mapOf("ValidPerson" to mapOf("types" to "com.enums")),
                    generateCustomAnnotations = true,
                    addDeprecatedAnnotation = true
                )
            ).generate()
        }
    }

    @Test
    fun `Use schema type when type name clashes with commonScalars`() {
        val schema = """
            type Price {
                amount: Double
                currency: Currency
                date: Date
            }
            enum Currency {
                EUR
                GBP
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[1].type.toString()).contains(basePackageName)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[2].type.toString()).isEqualTo("java.time.LocalDate")
    }

    @Test
    fun `The Upload scalar typeMapping works by default`() {
        val schema = """
            scalar Upload
            type Person {
                name: String
                age: Int
                uploadFile: Upload
            }
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(1)
        assertThat(dataTypes[0].typeSpec.fieldSpecs[2].type.toString()).isEqualTo("org.springframework.web.multipart.MultipartFile")
    }

    @Test
    fun `The default Upload scalar can be overridden`() {
        val schema = """
            scalar Upload
            type Person {
                name: String
                age: Int
                uploadFile: Upload
            }
        """.trimIndent()

        val (dataTypes, _) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "Upload" to "java.lang.Integer"
                )
            )
        ).generate()

        // Check that the Person type is generated
        assertThat(dataTypes.size).isEqualTo(1)

        // Check that the third field of the Person type is an Integer
        assertThat(dataTypes[0].typeSpec.fieldSpecs[2].type.toString()).isEqualTo("java.lang.Integer")
    }

    @Test
    fun `Can generate documentation`() {
        val schema = """
            type Query {
                getPersons(name: String!): [Person]
            }
            
            type Person @key(fields: "id") {
                id: ID!
                name: String
                age(unit: Unit): Int
            }
            
            enum Unit {
                seconds
                years
            }
        """.trimIndent()

        val codeGenResult: CodeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateDocs = true
            )
        ).generate()

        // Check that a docfile is generated
        assertThat(codeGenResult.docFiles.size).isEqualTo(2)
    }

    @Test
    fun `Supports typeMapping for union types`() {
        val schema = """
            type A {
                name: String
            }
        
            type B {
                count: Int
            }
        
            union C = A | B
        """.trimIndent()

        val (dataTypes) = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "C" to "java.lang.String"
                )
            )
        ).generate()

        assertThat(dataTypes.size).isEqualTo(2)

        assertThat(dataTypes[0].typeSpec.superinterfaces[0].toString()).isEqualTo("java.lang.String")
        assertThat(dataTypes[1].typeSpec.superinterfaces[0].toString()).isEqualTo("java.lang.String")
    }

    @Test
    fun `Supports typeMapping in union type generation`() {
        val schema = """
            type A {
                name: String
            }
        
            type B {
                count: Int
            }
        
            union C = A | B
        """.trimIndent()

        val result = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "A" to "java.lang.String"
                )
            )
        ).generate()

        assertThat(result.javaDataTypes.size).isEqualTo(1)

        assertThat(result.javaDataTypes[0].typeSpec.superinterfaces[0].toString()).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.types.C")
        assertCompilesJava(result)
    }

    @Test
    fun `The default value for Locale should be overridden and wrapped`() {
        val schema = """
            scalar Locale @specifiedBy(url:"https://tools.ietf.org/html/bcp47")

            input NameInput {
                  locale: Locale = "en-US"
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf(
                    "Locale" to "java.util.Locale"
                )
            )
        ).generate()

        val dataTypes = codeGenResult.javaDataTypes
        assertThat(dataTypes[0].typeSpec.fieldSpecs[0].initializer.toString()).isEqualTo("Locale.forLanguageTag(\"en-US\")")
        assertCompilesJava(dataTypes)
    }

    @Test
    fun `The default empty object value should result in constructor call`() {
        val schema = """
            input Movie {
                director: Person = {}
            }
            
            input Person {
                name: String = "Damian"
                age: Int = 33
            }
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate().javaDataTypes

        assertThat(dataTypes).hasSize(2)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("Movie")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("director")
        assertThat(colorField.initializer.toString()).isEqualTo("""new $typesPackageName.Person()""")

        assertCompilesJava(dataTypes)
    }

    @Test
    fun `The default object with properties should result in constructor call with args`() {
        val schema = """
            input Movie {
                director: Person = { name: "Harrison", car: { brand: "Ford" } }
            }

            input Person {
                name: String = "Damian"
                car: Car = { brand: "Tesla" }
            }

            input Car {
                brand: String = "VW"
            }
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate().javaDataTypes

        assertThat(dataTypes).hasSize(3)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("Movie")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("director")
        assertThat(colorField.initializer.toString()).isEqualTo(
            "new com.netflix.graphql.dgs.codegen.tests.generated.types.Person()" +
                """{{setName("Harrison");setCar(new com.netflix.graphql.dgs.codegen.tests.generated.types.Car(){{setBrand("Ford");}})""" +
                ";}}"
        )
        assertCompilesJava(dataTypes)
    }

    @Test
    fun `The default list value should support objects`() {
        val schema = """
            input Director {
                movies: [Movie!]! = [{ name: "Braveheart" }, { name: "Matrix", year: 1999 }]
            }

            input Movie {
                name: String = "Toy Story"
                year: Int = 1995
            }
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName
            )
        ).generate().javaDataTypes

        assertThat(dataTypes).hasSize(2)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("Director")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("movies")
        assertThat(colorField.initializer.toString()).isEqualTo(
            "java.util.Arrays.asList(" +
                "new com.netflix.graphql.dgs.codegen.tests.generated.types.Movie(){{setName(\"Braveheart\");}}, " +
                "new com.netflix.graphql.dgs.codegen.tests.generated.types.Movie(){{setName(\"Matrix\");setYear(1999);}}" +
                ")"
        )
        assertCompilesJava(dataTypes)
    }

    @Test
    fun `The default object value should call constructor from typeMapping`() {
        val schema = """
            input Movie {
                director: Person = { name: "Harrison" }
            }

            input Person {
                name: String = "Damian"
                age: Int = 33
            }
        """.trimIndent()

        val dataTypes = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                typeMapping = mapOf("Person" to "mypackage.Human")
            )
        ).generate().javaDataTypes

        assertThat(dataTypes).hasSize(1)

        val data = dataTypes[0]
        assertThat(data.packageName).isEqualTo(typesPackageName)

        val type = data.typeSpec
        assertThat(type.name).isEqualTo("Movie")

        val fields = type.fieldSpecs
        assertThat(fields).hasSize(1)

        val colorField = fields[0]
        assertThat(colorField.name).isEqualTo("director")
        assertThat(colorField.initializer.toString()).isEqualTo("new mypackage.Human(){{setName(\"Harrison\");}}")
    }

    @Test
    fun `Codegen should fail when default value specifies property does not exist in input type`() {
        val schema = """
            input Movie {
                director: Person = { firstname: "Harrison" }
            }
            
            input Person {
                name: String = "Damian"
            }
        """.trimIndent()

        val exception = assertThrows<IllegalStateException> {
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = basePackageName
                )
            ).generate()
        }
        assertThat(exception.message).isEqualTo("Property \"firstname\" does not exist in input type \"Person\"")
    }
}
