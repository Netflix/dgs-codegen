package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
public class Query(
  fruits: () -> List<Fruit?>? = fruitsDefault,
) {
  private val __fruits: () -> List<Fruit?>? = fruits

  @get:JvmName("getFruits")
  public val fruits: List<Fruit?>?
    get() = __fruits.invoke()

  public companion object {
    private val fruitsDefault: () -> List<Fruit?>? = 
        { throw IllegalStateException("Field `fruits` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var fruits: () -> List<Fruit?>? = fruitsDefault

    @JsonProperty("fruits")
    public fun withFruits(fruits: List<Fruit?>?): Builder = this.apply {
      this.fruits = { fruits }
    }

    public fun build(): Query = Query(
      fruits = fruits,
    )
  }
}
