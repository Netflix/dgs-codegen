package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__e === eDefault) == (other.__e === eDefault) && (__e === eDefault || Objects.equals(e,
        other.e)) &&
    (__es === esDefault) == (other.__es === esDefault) && (__es === esDefault || Objects.equals(es,
        other.es))
  }

  override fun hashCode(): Int = Objects.hash(if (__e === eDefault) eDefault else e,
  if (__es === esDefault) esDefault else es)

  @Generated
  public companion object {
    private val eDefault: () -> E? = 
        { throw IllegalStateException("Field `e` was not requested") }

    private val esDefault: () -> List<E?>? = 
        { throw IllegalStateException("Field `es` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
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
