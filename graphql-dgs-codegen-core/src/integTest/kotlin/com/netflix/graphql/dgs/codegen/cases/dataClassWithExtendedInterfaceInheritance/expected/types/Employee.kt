package com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Employee.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Employee.Builder::class)
public class Employee(
  firstname: () -> String = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  company: () -> String? = companyDefault,
  age: () -> Int = ageDefault,
) : Person {
  private val __firstname: () -> String = firstname

  private val __lastname: () -> String? = lastname

  private val __company: () -> String? = company

  private val __age: () -> Int = age

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  override val firstname: String
    get() = __firstname.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getLastname")
  override val lastname: String?
    get() = __lastname.invoke()

  @get:JvmName("getCompany")
  public val company: String?
    get() = __company.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getAge")
  override val age: Int
    get() = __age.invoke()

  public companion object {
    private val firstnameDefault: () -> String = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }

    private val companyDefault: () -> String? = 
        { throw IllegalStateException("Field `company` was not requested") }

    private val ageDefault: () -> Int = 
        { throw IllegalStateException("Field `age` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    private var company: () -> String? = companyDefault

    private var age: () -> Int = ageDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String): Builder = this.apply {
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

    @JsonProperty("age")
    public fun withAge(age: Int): Builder = this.apply {
      this.age = { age }
    }

    public fun build(): Employee = Employee(
      firstname = firstname,
      lastname = lastname,
      company = company,
      age = age,
    )
  }
}
