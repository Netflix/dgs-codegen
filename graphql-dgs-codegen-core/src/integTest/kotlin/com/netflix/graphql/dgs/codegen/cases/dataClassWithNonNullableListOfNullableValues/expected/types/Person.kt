package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableListOfNullableValues.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  name: () -> String = nameDefault,
  email: () -> List<String?> = emailDefault,
) {
  private val _name: () -> String = name

  private val _email: () -> List<String?> = email

  @get:JvmName("getName")
  public val name: String
    get() = _name.invoke()

  @get:JvmName("getEmail")
  public val email: List<String?>
    get() = _email.invoke()

  public companion object {
    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }


    private val emailDefault: () -> List<String?> = 
        { throw IllegalStateException("Field `email` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String = nameDefault

    private var email: () -> List<String?> = emailDefault

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("email")
    public fun withEmail(email: List<String?>): Builder = this.apply {
      this.email = { email }
    }

    public fun build(): Person = Person(
      name = name,
      email = email,
    )
  }
}
