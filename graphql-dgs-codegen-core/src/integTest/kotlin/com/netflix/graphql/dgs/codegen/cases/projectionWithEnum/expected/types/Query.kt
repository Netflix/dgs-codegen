package com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.projectionWithEnum.expected.Generated
import java.lang.IllegalStateException
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

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__e === eDefault) eDefault else e,
      if (__es === esDefault) esDefault else es,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__e === eDefault) null else "e=" + e,
      if (__es === esDefault) null else "es=" + es,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

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
