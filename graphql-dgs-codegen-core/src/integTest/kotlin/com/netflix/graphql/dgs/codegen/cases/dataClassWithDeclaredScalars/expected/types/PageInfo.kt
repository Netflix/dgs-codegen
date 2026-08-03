package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = PageInfo.Builder::class)
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PageInfo) return false
    return (__startCursor === startCursorDefault) == (other.__startCursor === startCursorDefault) &&
        (__startCursor === startCursorDefault || Objects.equals(startCursor, other.startCursor)) &&
    (__endCursor === endCursorDefault) == (other.__endCursor === endCursorDefault) && (__endCursor
        === endCursorDefault || Objects.equals(endCursor, other.endCursor)) &&
    (__hasNextPage === hasNextPageDefault) == (other.__hasNextPage === hasNextPageDefault) &&
        (__hasNextPage === hasNextPageDefault || Objects.equals(hasNextPage, other.hasNextPage)) &&
    (__hasPreviousPage === hasPreviousPageDefault) == (other.__hasPreviousPage ===
        hasPreviousPageDefault) && (__hasPreviousPage === hasPreviousPageDefault ||
        Objects.equals(hasPreviousPage, other.hasPreviousPage))
  }

  override fun hashCode(): Int = Objects.hash(if (__startCursor === startCursorDefault)
      startCursorDefault else startCursor,
  if (__endCursor === endCursorDefault) endCursorDefault else endCursor,
  if (__hasNextPage === hasNextPageDefault) hasNextPageDefault else hasNextPage,
  if (__hasPreviousPage === hasPreviousPageDefault) hasPreviousPageDefault else hasPreviousPage)

  override fun toString(): String = listOfNotNull(if (__startCursor === startCursorDefault) null
      else "startCursor=" + startCursor, if (__endCursor === endCursorDefault) null else
      "endCursor=" + endCursor, if (__hasNextPage === hasNextPageDefault) null else "hasNextPage=" +
      hasNextPage, if (__hasPreviousPage === hasPreviousPageDefault) null else "hasPreviousPage=" +
      hasPreviousPage).joinToString(prefix = "PageInfo(", postfix = ")")

  @Generated
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

  @Generated
  @JsonPOJOBuilder
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
