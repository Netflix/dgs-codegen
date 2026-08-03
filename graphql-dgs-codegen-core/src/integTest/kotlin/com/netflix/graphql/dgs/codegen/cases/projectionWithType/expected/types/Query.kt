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
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  person: () -> Person? = personDefault,
  people: () -> List<Person?>? = peopleDefault,
) {
  private val __person: () -> Person? = person

  private val __people: () -> List<Person?>? = people

  @get:JvmName("getPerson")
  public val person: Person?
    get() = __person.invoke()

  @get:JvmName("getPeople")
  public val people: List<Person?>?
    get() = __people.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__person === personDefault) == (other.__person === personDefault) && (__person ===
        personDefault || Objects.equals(person, other.person)) &&
    (__people === peopleDefault) == (other.__people === peopleDefault) && (__people ===
        peopleDefault || Objects.equals(people, other.people))
  }

  override fun hashCode(): Int = Objects.hash(if (__person === personDefault) personDefault else
      person,
  if (__people === peopleDefault) peopleDefault else people)

  override fun toString(): String = listOfNotNull(if (__person === personDefault) null else
      "person=" + person, if (__people === peopleDefault) null else "people=" +
      people).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val personDefault: () -> Person? = 
        { throw IllegalStateException("Field `person` was not requested") }

    private val peopleDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `people` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var person: () -> Person? = personDefault

    private var people: () -> List<Person?>? = peopleDefault

    @JsonProperty("person")
    public fun withPerson(person: Person?): Builder = this.apply {
      this.person = { person }
    }

    @JsonProperty("people")
    public fun withPeople(people: List<Person?>?): Builder = this.apply {
      this.people = { people }
    }

    public fun build(): Query = Query(
      person = person,
      people = people,
    )
  }
}
