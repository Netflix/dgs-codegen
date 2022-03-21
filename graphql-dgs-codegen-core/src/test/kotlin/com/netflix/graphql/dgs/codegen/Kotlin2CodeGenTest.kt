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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Kotlin2CodeGenTest {

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN2,
            )
        ).generate()

        val dataTypes = codeGenResult.kotlinDataTypes

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  people: () -> List<Person?>? = peopleDefault
) {
  private val _people: () -> List<Person?>? = people

  public val people: List<Person?>?
    get() = _people.invoke()

  public companion object {
    private val peopleDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `people` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var people: () -> List<Person?>? = peopleDefault

    @JsonProperty("people")
    public fun withPeople(people: List<Person?>?): Builder = this.apply {
      this.people = { people }
    }

    public fun build() = Query(
      people = people,
    )
  }
}
""".trimStart(),
            dataTypes[0].toString()
        )

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault
) {
  private val _firstname: () -> String? = firstname

  private val _lastname: () -> String? = lastname

  public val firstname: String?
    get() = _firstname.invoke()

  public val lastname: String?
    get() = _lastname.invoke()

  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }


    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String? = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String?): Builder = this.apply {
      this.firstname = { firstname }
    }

    @JsonProperty("lastname")
    public fun withLastname(lastname: String?): Builder = this.apply {
      this.lastname = { lastname }
    }

    public fun build() = Person(
      firstname = firstname,
      lastname = lastname,
    )
  }
}
""".trimStart(),
            dataTypes[1].toString()
        )

        assertCompilesKotlin(codeGenResult)
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
                language = Language.KOTLIN2,
            )
        ).generate()

        val dataTypes = codeGenResult.kotlinDataTypes
        val interfaces = codeGenResult.kotlinInterfaces

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename"
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Employee::class, name = "Employee")
])
public interface Person {
  public val firstname: String?

  public val lastname: String?
}
""".trimStart(),
            interfaces[0].toString()
        )

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Employee.Builder::class)
public class Employee(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  company: () -> String? = companyDefault
) : Person {
  private val _firstname: () -> String? = firstname

  private val _lastname: () -> String? = lastname

  private val _company: () -> String? = company

  public override val firstname: String?
    get() = _firstname.invoke()

  public override val lastname: String?
    get() = _lastname.invoke()

  public val company: String?
    get() = _company.invoke()

  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }


    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }


    private val companyDefault: () -> String? = 
        { throw IllegalStateException("Field `company` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String? = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    private var company: () -> String? = companyDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String?): Builder = this.apply {
      this.firstname = { firstname }
    }

    @JsonProperty("lastname")
    public fun withLastname(lastname: String?): Builder = this.apply {
      this.lastname = { lastname }
    }

    @JsonProperty("company")
    public fun withCompany(company: String?): Builder = this.apply {
      this.company = { company }
    }

    public fun build() = Employee(
      firstname = firstname,
      lastname = lastname,
      company = company,
    )
  }
}
""".trimStart(),
            dataTypes[1].toString()
        )

        assertCompilesKotlin(codeGenResult)
    }

    @Test
    fun generateEnum() {

        val schema = """
            type Query {
                types: [EmployeeTypes]
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
                language = Language.KOTLIN2,
            )
        ).generate()

        val enumTypes = codeGenResult.kotlinEnumTypes

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

public enum class EmployeeTypes {
  ENGINEER,
  MANAGER,
  DIRECTOR,
}
""".trimStart(),
            enumTypes[0].toString()
        )

        assertCompilesKotlin(codeGenResult)
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN2,
            )
        ).generate()

        val inputTypes = codeGenResult.kotlinInputTypes

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.types

import com.netflix.graphql.dgs.client.codegen.GraphQLInput
import kotlin.String

public class MovieFilter(
  public val genre: String? = default("genre")
) : GraphQLInput()
""".trimStart(),
            inputTypes[0].toString()
        )

        assertCompilesKotlin(codeGenResult)
    }

    @Test
    fun generateClientTypes() {

        val schema = """
            type Query {
                person: Person
                people: [Person]
                string: String
                strings: [String]
                e: E
                es: [E]
                personA(a1: String, a2: String!, a3: I): Person
                stringA(a1: String, a2: String!, a3: I): String
            }

            interface Person {
                firstname: String
            }

            type Employee implements Person {
                firstname: String
                company: String
            }
            
            enum E {
                V
            }
            
            input I {
                arg: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                language = Language.KOTLIN2,
                generateClientApi = true,
            )
        ).generate()

        val clientTypes = codeGenResult.kotlinClientTypes

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.client

import kotlin.String

public object Client {
  public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String {
    val projection = QueryProjection()
    _projection.invoke(projection)
    return "query ${'$'}{projection.asQuery()}"
  }
}
""".trimStart(),
            clientTypes[4].toString()
        )

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import com.netflix.graphql.dgs.codegen.tests.generated.types.I
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public val string: QueryProjection
    get() {
      field("string")
      return this
    }

  public val strings: QueryProjection
    get() {
      field("strings")
      return this
    }

  public val e: QueryProjection
    get() {
      field("e")
      return this
    }

  public val es: QueryProjection
    get() {
      field("es")
      return this
    }

  public fun person(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("person", PersonProjection(), _projection)
    return this
  }

  public fun people(_projection: PersonProjection.() -> PersonProjection): QueryProjection {
    project("people", PersonProjection(), _projection)
    return this
  }

  public fun personA(
    a1: String? = default("a1"),
    a2: String,
    a3: I? = default("a3"),
    _projection: PersonProjection.() -> PersonProjection
  ): QueryProjection {
    val args = formatArgs("a1" to a1, "a2" to a2, "a3" to a3)
    project("personA(${'$'}args)", PersonProjection(), _projection)
    return this
  }

  public fun stringA(
    a1: String? = default("a1"),
    a2: String,
    a3: I? = default("a3")
  ): QueryProjection {
    val args = formatArgs("a1" to a1, "a2" to a2, "a3" to a3)
    field("stringA(${'$'}args)")
    return this
  }
}
""".trimStart(),
            clientTypes[0].toString()
        )

        assertEquals(
            """
package com.netflix.graphql.dgs.codegen.tests.generated.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
  public val firstname: PersonProjection
    get() {
      field("firstname")
      return this
    }

  public fun onEmployee(_projection: EmployeeProjection.() -> EmployeeProjection):
      PersonProjection {
    project("... on Employee", EmployeeProjection(), _projection)
    return this
  }
}
""".trimStart(),
            clientTypes[3].toString()
        )

        assertCompilesKotlin(codeGenResult)
    }
}
