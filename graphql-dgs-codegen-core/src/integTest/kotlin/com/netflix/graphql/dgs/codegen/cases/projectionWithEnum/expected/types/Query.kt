package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.types

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
  e: () -> E? = eDefault,
  es: () -> List<E?>? = esDefault,
) {
  private val __e: () -> E? = e

  private val __es: () -> List<E?>? = es

  @get:JvmName("getE")
  public val e: E?
    get() = __e.invoke()

  @get:JvmName("getEs")
  public val es: List<E?>?
    get() = __es.invoke()

  public companion object {
    private val eDefault: () -> E? = 
        { throw IllegalStateException("Field `e` was not requested") }

    private val esDefault: () -> List<E?>? = 
        { throw IllegalStateException("Field `es` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var e: () -> E? = eDefault

    private var es: () -> List<E?>? = esDefault

    @JsonProperty("e")
    public fun withE(e: E?): Builder = this.apply {
      this.e = { e }
    }

    @JsonProperty("es")
    public fun withEs(es: List<E?>?): Builder = this.apply {
      this.es = { es }
    }

    public fun build(): Query = Query(
      e = e,
      es = es,
    )
  }
}
