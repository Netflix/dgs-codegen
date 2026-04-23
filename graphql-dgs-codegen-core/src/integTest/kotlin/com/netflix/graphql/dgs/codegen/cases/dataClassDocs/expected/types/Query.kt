package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> Movie? = searchDefault,
) {
  private val __search: () -> Movie? = search

  @get:JvmName("getSearch")
  public val search: Movie?
    get() = __search.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val searchDefault: () -> Movie? = 
        { throw IllegalStateException("Field `search` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
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
