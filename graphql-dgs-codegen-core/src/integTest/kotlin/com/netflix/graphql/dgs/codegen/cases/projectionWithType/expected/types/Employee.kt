package com.netflix.graphql.dgs.codegen.cases.projectionWithType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithType.expected.Generated
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
  firstname: () -> String? = firstnameDefault,
  company: () -> String? = companyDefault,
) : Person {
  private val __firstname: () -> String? = firstname

  private val __company: () -> String? = company

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  override val firstname: String?
    get() = __firstname.invoke()

  @get:JvmName("getCompany")
  public val company: String?
    get() = __company.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Employee) return false
    return (__firstname === firstnameDefault) == (other.__firstname === firstnameDefault) &&
        (__firstname === firstnameDefault || Objects.equals(firstname, other.firstname)) &&
    (__company === companyDefault) == (other.__company === companyDefault) && (__company ===
        companyDefault || Objects.equals(company, other.company))
  }

  override fun hashCode(): Int = Objects.hash(if (__firstname === firstnameDefault) firstnameDefault
      else firstname,
  if (__company === companyDefault) companyDefault else company)

  override fun toString(): String = listOfNotNull(if (__firstname === firstnameDefault) null else
      "firstname=" + firstname, if (__company === companyDefault) null else "company=" +
      company).joinToString(prefix = "Employee(", postfix = ")")

  @Generated
  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val companyDefault: () -> String? = 
        { throw IllegalStateException("Field `company` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String? = firstnameDefault

    private var company: () -> String? = companyDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String?): Builder = this.apply {
      this.firstname = { firstname }
    }

    @JsonProperty("company")
    public fun withCompany(company: String?): Builder = this.apply {
      this.company = { company }
    }

    public fun build(): Employee = Employee(
      firstname = firstname,
      company = company,
    )
  }
}
