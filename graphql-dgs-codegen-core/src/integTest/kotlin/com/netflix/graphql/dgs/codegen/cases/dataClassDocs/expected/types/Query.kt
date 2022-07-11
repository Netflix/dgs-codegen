package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> Movie? = searchDefault,
) {
  private val _search: () -> Movie? = search

  public val search: Movie?
    get() = _search.invoke()

  public companion object {
    private val searchDefault: () -> Movie? = 
        { throw IllegalStateException("Field `search` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var search: () -> Movie? = searchDefault

    @JsonProperty("search")
    public fun withSearch(search: Movie?): Builder = this.apply {
      this.search = { search }
    }

    public fun build() = Query(
      search = search,
    )
  }
}
