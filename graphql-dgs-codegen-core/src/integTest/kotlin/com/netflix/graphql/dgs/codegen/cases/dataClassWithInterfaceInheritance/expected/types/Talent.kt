package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.types

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
@JsonDeserialize(builder = Talent.Builder::class)
public class Talent(
  firstname: () -> String = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  company: () -> String? = companyDefault,
  imdbProfile: () -> String? = imdbProfileDefault,
) : Employee {
  private val __firstname: () -> String = firstname

  private val __lastname: () -> String? = lastname

  private val __company: () -> String? = company

  private val __imdbProfile: () -> String? = imdbProfile

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFirstname")
  override val firstname: String
    get() = __firstname.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getLastname")
  override val lastname: String?
    get() = __lastname.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getCompany")
  override val company: String?
    get() = __company.invoke()

  @get:JvmName("getImdbProfile")
  public val imdbProfile: String?
    get() = __imdbProfile.invoke()

  public companion object {
    private val firstnameDefault: () -> String = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }

    private val companyDefault: () -> String? = 
        { throw IllegalStateException("Field `company` was not requested") }

    private val imdbProfileDefault: () -> String? = 
        { throw IllegalStateException("Field `imdbProfile` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    private var company: () -> String? = companyDefault

    private var imdbProfile: () -> String? = imdbProfileDefault

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

    @JsonProperty("imdbProfile")
    public fun withImdbProfile(imdbProfile: String?): Builder = this.apply {
      this.imdbProfile = { imdbProfile }
    }

    public fun build(): Talent = Talent(
      firstname = firstname,
      lastname = lastname,
      company = company,
      imdbProfile = imdbProfile,
    )
  }
}
