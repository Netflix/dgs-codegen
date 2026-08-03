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
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = EntityConnection.Builder::class)
public class EntityConnection(
  pageInfo: () -> PageInfo = pageInfoDefault,
  edges: () -> List<EntityEdge?>? = edgesDefault,
) {
  private val __pageInfo: () -> PageInfo = pageInfo

  private val __edges: () -> List<EntityEdge?>? = edges

  @get:JvmName("getPageInfo")
  public val pageInfo: PageInfo
    get() = __pageInfo.invoke()

  @get:JvmName("getEdges")
  public val edges: List<EntityEdge?>?
    get() = __edges.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is EntityConnection) return false
    return (__pageInfo === pageInfoDefault) == (other.__pageInfo === pageInfoDefault) && (__pageInfo
        === pageInfoDefault || Objects.equals(pageInfo, other.pageInfo)) &&
    (__edges === edgesDefault) == (other.__edges === edgesDefault) && (__edges === edgesDefault ||
        Objects.equals(edges, other.edges))
  }

  override fun hashCode(): Int = Objects.hash(if (__pageInfo === pageInfoDefault) pageInfoDefault
      else pageInfo,
  if (__edges === edgesDefault) edgesDefault else edges)

  @Generated
  public companion object {
    private val pageInfoDefault: () -> PageInfo = 
        { throw IllegalStateException("Field `pageInfo` was not requested") }

    private val edgesDefault: () -> List<EntityEdge?>? = 
        { throw IllegalStateException("Field `edges` was not requested") }
  }

  @Generated
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
