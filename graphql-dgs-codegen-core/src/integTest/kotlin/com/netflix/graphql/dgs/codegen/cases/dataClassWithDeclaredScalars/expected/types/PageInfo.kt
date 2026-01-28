package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.Boolean
import kotlin.String
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = PageInfo.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = PageInfo.Builder::class)
public class PageInfo(
  startCursor: () -> String? = startCursorDefault,
  endCursor: () -> String? = endCursorDefault,
  hasNextPage: () -> Boolean = hasNextPageDefault,
  hasPreviousPage: () -> Boolean = hasPreviousPageDefault,
) {
  private val __startCursor: () -> String? = startCursor

  private val __endCursor: () -> String? = endCursor

  private val __hasNextPage: () -> Boolean = hasNextPage

  private val __hasPreviousPage: () -> Boolean = hasPreviousPage

  @get:JvmName("getStartCursor")
  public val startCursor: String?
    get() = __startCursor.invoke()

  @get:JvmName("getEndCursor")
  public val endCursor: String?
    get() = __endCursor.invoke()

  @get:JvmName("getHasNextPage")
  public val hasNextPage: Boolean
    get() = __hasNextPage.invoke()

  @get:JvmName("getHasPreviousPage")
  public val hasPreviousPage: Boolean
    get() = __hasPreviousPage.invoke()

  public companion object {
    private val startCursorDefault: () -> String? = 
        { throw IllegalStateException("Field `startCursor` was not requested") }

    private val endCursorDefault: () -> String? = 
        { throw IllegalStateException("Field `endCursor` was not requested") }

    private val hasNextPageDefault: () -> Boolean = 
        { throw IllegalStateException("Field `hasNextPage` was not requested") }

    private val hasPreviousPageDefault: () -> Boolean = 
        { throw IllegalStateException("Field `hasPreviousPage` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var startCursor: () -> String? = startCursorDefault

    private var endCursor: () -> String? = endCursorDefault

    private var hasNextPage: () -> Boolean = hasNextPageDefault

    private var hasPreviousPage: () -> Boolean = hasPreviousPageDefault

    @JsonProperty("startCursor")
    public fun withStartCursor(startCursor: String?): Builder = this.apply {
      this.startCursor = { startCursor }
    }

    @JsonProperty("endCursor")
    public fun withEndCursor(endCursor: String?): Builder = this.apply {
      this.endCursor = { endCursor }
    }

    @JsonProperty("hasNextPage")
    public fun withHasNextPage(hasNextPage: Boolean): Builder = this.apply {
      this.hasNextPage = { hasNextPage }
    }

    @JsonProperty("hasPreviousPage")
    public fun withHasPreviousPage(hasPreviousPage: Boolean): Builder = this.apply {
      this.hasPreviousPage = { hasPreviousPage }
    }

    public fun build(): PageInfo = PageInfo(
      startCursor = startCursor,
      endCursor = endCursor,
      hasNextPage = hasNextPage,
      hasPreviousPage = hasPreviousPage,
    )
  }
}
