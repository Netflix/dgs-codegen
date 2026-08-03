package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> Movie? = searchDefault,
) {
  private val __search: () -> Movie? = search

  @get:JvmName("getSearch")
  public val search: Movie?
    get() = __search.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__search === searchDefault) == (other.__search === searchDefault) && (__search ===
        searchDefault || Objects.equals(search, other.search))
  }

  override fun hashCode(): Int = Objects.hash(if (__search === searchDefault) searchDefault else
      search)

  override fun toString(): String = listOfNotNull(if (__search === searchDefault) null else
      "search=" + search).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val searchDefault: () -> Movie? = 
        { throw IllegalStateException("Field `search` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var search: () -> Movie? = searchDefault

    @JsonProperty("search")
    public fun withSearch(search: Movie?): Builder = this.apply {
      this.search = { search }
    }

    public fun build(): Query = Query(
      search = search,
    )
  }
}
