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
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  people: () -> List<Person>? = peopleDefault,
) {
  private val __people: () -> List<Person>? = people

  @get:JvmName("getPeople")
  public val people: List<Person>?
    get() = __people.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__people === peopleDefault) == (other.__people === peopleDefault) && (__people ===
        peopleDefault || Objects.equals(people, other.people))
  }

  override fun hashCode(): Int = Objects.hash(if (__people === peopleDefault) peopleDefault else
      people)

  override fun toString(): String = listOfNotNull(if (__people === peopleDefault) null else
      "people=" + people).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val peopleDefault: () -> List<Person>? = 
        { throw IllegalStateException("Field `people` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var people: () -> List<Person>? = peopleDefault

    @JsonProperty("people")
    public fun withPeople(people: List<Person>?): Builder = this.apply {
      this.people = { people }
    }

    public fun build(): Query = Query(
      people = people,
    )
  }
}
