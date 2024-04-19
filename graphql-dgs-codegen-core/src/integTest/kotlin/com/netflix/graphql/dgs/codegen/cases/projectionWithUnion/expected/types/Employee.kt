package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Employee.Builder::class)
public class Employee(
  firstname: () -> String? = firstnameDefault,
  company: () -> String? = companyDefault,
) : Person, U {
  private val __firstname: () -> String? = firstname

  private val __company: () -> String? = company

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  override val firstname: String?
    get() = __firstname.invoke()

  @get:JvmName("getCompany")
  public val company: String?
    get() = __company.invoke()

  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }


    private val companyDefault: () -> String? = 
        { throw IllegalStateException("Field `company` was not requested") }

  }

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
