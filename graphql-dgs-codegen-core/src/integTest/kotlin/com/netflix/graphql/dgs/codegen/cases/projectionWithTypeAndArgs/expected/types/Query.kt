package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  person: () -> Person? = personDefault,
) {
  private val __person: () -> Person? = person

  @get:JvmName("getPerson")
  public val person: Person?
    get() = __person.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__person === personDefault) == (other.__person === personDefault) && (__person ===
        personDefault || Objects.equals(person, other.person))
  }

  override fun hashCode(): Int = Objects.hash(if (__person === personDefault) personDefault else
      person)

  @Generated
  public companion object {
    private val personDefault: () -> Person? = 
        { throw IllegalStateException("Field `person` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var person: () -> Person? = personDefault

    @JsonProperty("person")
    public fun withPerson(person: Person?): Builder = this.apply {
      this.person = { person }
    }

    public fun build(): Query = Query(
      person = person,
    )
  }
}
