package com.netflix.graphql.dgs.codegen.cases.union.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.union.expected.Generated
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> List<SearchResult?>? = searchDefault,
) {
  private val __search: () -> List<SearchResult?>? = search

  @get:JvmName("getSearch")
  public val search: List<SearchResult?>?
    get() = __search.invoke()

  @Generated
  public companion object {
    private val searchDefault: () -> List<SearchResult?>? = 
        { throw IllegalStateException("Field `search` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var search: () -> List<SearchResult?>? = searchDefault

    @JsonProperty("search")
    public fun withSearch(search: List<SearchResult?>?): Builder = this.apply {
      this.search = { search }
    }

    public fun build(): Query = Query(
      search = search,
    )
  }
}
