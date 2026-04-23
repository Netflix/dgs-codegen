package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  person: () -> Person? = personDefault,
) {
  private val __person: () -> Person? = person

  @get:JvmName("getPerson")
  public val person: Person?
    get() = __person.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val personDefault: () -> Person? = 
        { throw IllegalStateException("Field `person` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
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
