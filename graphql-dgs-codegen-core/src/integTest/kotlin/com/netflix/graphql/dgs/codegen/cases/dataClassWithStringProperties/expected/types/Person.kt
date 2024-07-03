package com.netflix.graphql.dgs.codegen.cases.dataClassWithStringProperties.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

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

    public fun build(): Person = Person(
      firstname = firstname,
      lastname = lastname,
    )
  }
}
