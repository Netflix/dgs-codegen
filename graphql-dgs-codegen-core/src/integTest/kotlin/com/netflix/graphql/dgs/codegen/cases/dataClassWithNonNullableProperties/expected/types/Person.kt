package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableProperties.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableProperties.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
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
  name: () -> String = nameDefault,
  email: () -> List<String> = emailDefault,
) {
  private val __name: () -> String = name

  private val __email: () -> List<String> = email

  @get:JvmName("getName")
  public val name: String
    get() = __name.invoke()

  @get:JvmName("getEmail")
  public val email: List<String>
    get() = __email.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Person) return false
    return (__name === nameDefault) == (other.__name === nameDefault) && (__name === nameDefault ||
        Objects.equals(name, other.name)) &&
    (__email === emailDefault) == (other.__email === emailDefault) && (__email === emailDefault ||
        Objects.equals(email, other.email))
  }

  override fun hashCode(): Int = Objects.hash(if (__name === nameDefault) nameDefault else name,
  if (__email === emailDefault) emailDefault else email)

  override fun toString(): String = listOfNotNull(if (__name === nameDefault) null else "name=" +
      name, if (__email === emailDefault) null else "email=" + email).joinToString(prefix =
      "Person(", postfix = ")")

  @Generated
  public companion object {
    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val emailDefault: () -> List<String> = 
        { throw IllegalStateException("Field `email` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String = nameDefault

    private var email: () -> List<String> = emailDefault

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("email")
    public fun withEmail(email: List<String>): Builder = this.apply {
      this.email = { email }
    }

    public fun build(): Person = Person(
      name = name,
      email = email,
    )
  }
}
