package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  person: () -> Person? = personDefault,
) {
  private val _person: () -> Person? = person

  public val person: Person?
    get() = _person.invoke()

  public companion object {
    private val personDefault: () -> Person? = 
        { throw IllegalStateException("Field `person` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var person: () -> Person? = personDefault

    @JsonProperty("person")
    public fun withPerson(person: Person?): Builder = this.apply {
      this.person = { person }
    }

    public fun build() = Query(
      person = person,
    )
  }
}
