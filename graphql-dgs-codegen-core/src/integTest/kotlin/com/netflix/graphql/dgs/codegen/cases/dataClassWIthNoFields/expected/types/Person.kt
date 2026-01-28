package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
public class Person() {
  public companion object

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    public fun build(): Person = Person(
    )
  }
}
