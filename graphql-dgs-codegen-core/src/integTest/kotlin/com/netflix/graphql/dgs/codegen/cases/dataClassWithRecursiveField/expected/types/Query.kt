package com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  people: () -> List<Person?>? = peopleDefault,
) {
  private val __people: () -> List<Person?>? = people

  @get:JvmName("getPeople")
  public val people: List<Person?>?
    get() = __people.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val peopleDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `people` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var people: () -> List<Person?>? = peopleDefault

    @JsonProperty("people")
    public fun withPeople(people: List<Person?>?): Builder = this.apply {
      this.people = { people }
    }

    public fun build(): Query = Query(
      people = people,
    )
  }
}
