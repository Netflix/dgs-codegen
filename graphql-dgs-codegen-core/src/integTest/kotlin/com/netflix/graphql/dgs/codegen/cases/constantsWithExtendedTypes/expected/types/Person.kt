package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedTypes.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  email: () -> String? = emailDefault,
) {
  private val __firstname: () -> String? = firstname

  private val __lastname: () -> String? = lastname

  private val __email: () -> String? = email

  @get:JvmName("getFirstname")
  public val firstname: String?
    get() = __firstname.invoke()

  @get:JvmName("getLastname")
  public val lastname: String?
    get() = __lastname.invoke()

  @get:JvmName("getEmail")
  public val email: String?
    get() = __email.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Person) return false
    return (__firstname === firstnameDefault) == (other.__firstname === firstnameDefault) &&
        (__firstname === firstnameDefault || Objects.equals(firstname, other.firstname)) &&
    (__lastname === lastnameDefault) == (other.__lastname === lastnameDefault) && (__lastname ===
        lastnameDefault || Objects.equals(lastname, other.lastname)) &&
    (__email === emailDefault) == (other.__email === emailDefault) && (__email === emailDefault ||
        Objects.equals(email, other.email))
  }

  override fun hashCode(): Int = Objects.hash(if (__firstname === firstnameDefault) firstnameDefault
      else firstname,
  if (__lastname === lastnameDefault) lastnameDefault else lastname,
  if (__email === emailDefault) emailDefault else email)

  override fun toString(): String = listOfNotNull(if (__firstname === firstnameDefault) null else
      "firstname=" + firstname, if (__lastname === lastnameDefault) null else "lastname=" +
      lastname, if (__email === emailDefault) null else "email=" + email).joinToString(prefix =
      "Person(", postfix = ")")

  @Generated
  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }

    private val emailDefault: () -> String? = 
        { throw IllegalStateException("Field `email` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String? = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    private var email: () -> String? = emailDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String?): Builder = this.apply {
      this.firstname = { firstname }
    }

    @JsonProperty("lastname")
    public fun withLastname(lastname: String?): Builder = this.apply {
      this.lastname = { lastname }
    }

    @JsonProperty("email")
    public fun withEmail(email: String?): Builder = this.apply {
      this.email = { email }
    }

    public fun build(): Person = Person(
      firstname = firstname,
      lastname = lastname,
      email = email,
    )
  }
}
