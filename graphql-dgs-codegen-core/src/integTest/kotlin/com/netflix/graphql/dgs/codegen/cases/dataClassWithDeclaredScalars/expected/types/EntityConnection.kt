package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = EntityConnection.Builder::class)
public class EntityConnection(
  pageInfo: () -> PageInfo = pageInfoDefault,
  edges: () -> List<EntityEdge?>? = edgesDefault,
) {
  private val _pageInfo: () -> PageInfo = pageInfo

  private val _edges: () -> List<EntityEdge?>? = edges

  @get:JvmName("getPageInfo")
  public val pageInfo: PageInfo
    get() = _pageInfo.invoke()

  @get:JvmName("getEdges")
  public val edges: List<EntityEdge?>?
    get() = _edges.invoke()

  public companion object {
    private val pageInfoDefault: () -> PageInfo = 
        { throw IllegalStateException("Field `pageInfo` was not requested") }


    private val edgesDefault: () -> List<EntityEdge?>? = 
        { throw IllegalStateException("Field `edges` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var pageInfo: () -> PageInfo = pageInfoDefault

    private var edges: () -> List<EntityEdge?>? = edgesDefault

    @JsonProperty("pageInfo")
    public fun withPageInfo(pageInfo: PageInfo): Builder = this.apply {
      this.pageInfo = { pageInfo }
    }

    @JsonProperty("edges")
    public fun withEdges(edges: List<EntityEdge?>?): Builder = this.apply {
      this.edges = { edges }
    }

    public fun build(): EntityConnection = EntityConnection(
      pageInfo = pageInfo,
      edges = edges,
    )
  }
}
