package com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.constantsForInputTypes.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
) {
  private val __firstname: () -> String? = firstname

  private val __lastname: () -> String? = lastname

  @get:JvmName("getFirstname")
  public val firstname: String?
    get() = __firstname.invoke()

  @get:JvmName("getLastname")
  public val lastname: String?
    get() = __lastname.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__firstname === firstnameDefault) firstnameDefault else firstname,
      if (__lastname === lastnameDefault) lastnameDefault else lastname,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Person &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__firstname === firstnameDefault) null else "firstname=" + firstname,
      if (__lastname === lastnameDefault) null else "lastname=" + lastname,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Person(", postfix =
      ")")

  @Generated
  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }
  }

  @Generated
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

    public fun build(): Person = Person(
      firstname = firstname,
      lastname = lastname,
    )
  }
}
