package com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Employee.Builder::class)
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Employee) return false
    return (__firstname === firstnameDefault) == (other.__firstname === firstnameDefault) &&
        (__firstname === firstnameDefault || Objects.equals(firstname, other.firstname)) &&
    (__lastname === lastnameDefault) == (other.__lastname === lastnameDefault) && (__lastname ===
        lastnameDefault || Objects.equals(lastname, other.lastname)) &&
    (__company === companyDefault) == (other.__company === companyDefault) && (__company ===
        companyDefault || Objects.equals(company, other.company)) &&
    (__age === ageDefault) == (other.__age === ageDefault) && (__age === ageDefault ||
        Objects.equals(age, other.age))
  }

  override fun hashCode(): Int = Objects.hash(if (__firstname === firstnameDefault) firstnameDefault
      else firstname,
  if (__lastname === lastnameDefault) lastnameDefault else lastname,
  if (__company === companyDefault) companyDefault else company,
  if (__age === ageDefault) ageDefault else age)

  override fun toString(): String = listOfNotNull(if (__firstname === firstnameDefault) null else
      "firstname=" + firstname, if (__lastname === lastnameDefault) null else "lastname=" +
      lastname, if (__company === companyDefault) null else "company=" + company, if (__age ===
      ageDefault) null else "age=" + age).joinToString(prefix = "Employee(", postfix = ")")

  @Generated
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

  @Generated
  @JsonPOJOBuilder
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
