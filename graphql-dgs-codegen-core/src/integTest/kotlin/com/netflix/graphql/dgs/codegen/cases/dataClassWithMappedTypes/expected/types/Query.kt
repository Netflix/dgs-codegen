package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.Generated
import graphql.relay.SimpleListConnection
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
  entity: () -> List<Entity?>? = entityDefault,
  entityConnection: () -> SimpleListConnection<EntityEdge>? = entityConnectionDefault,
) {
  private val __entity: () -> List<Entity?>? = entity

  private val __entityConnection: () -> SimpleListConnection<EntityEdge>? = entityConnection

  @get:JvmName("getEntity")
  public val entity: List<Entity?>?
    get() = __entity.invoke()

  @get:JvmName("getEntityConnection")
  public val entityConnection: SimpleListConnection<EntityEdge>?
    get() = __entityConnection.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__entity === entityDefault) == (other.__entity === entityDefault) && (__entity ===
        entityDefault || Objects.equals(entity, other.entity)) &&
    (__entityConnection === entityConnectionDefault) == (other.__entityConnection ===
        entityConnectionDefault) && (__entityConnection === entityConnectionDefault ||
        Objects.equals(entityConnection, other.entityConnection))
  }

  override fun hashCode(): Int = Objects.hash(if (__entity === entityDefault) entityDefault else
      entity,
  if (__entityConnection === entityConnectionDefault) entityConnectionDefault else entityConnection)

  @Generated
  public companion object {
    private val entityDefault: () -> List<Entity?>? = 
        { throw IllegalStateException("Field `entity` was not requested") }

    private val entityConnectionDefault: () -> SimpleListConnection<EntityEdge>? = 
        { throw IllegalStateException("Field `entityConnection` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var entity: () -> List<Entity?>? = entityDefault

    private var entityConnection: () -> SimpleListConnection<EntityEdge>? = entityConnectionDefault

    @JsonProperty("entity")
    public fun withEntity(entity: List<Entity?>?): Builder = this.apply {
      this.entity = { entity }
    }

    @JsonProperty("entityConnection")
    public fun withEntityConnection(entityConnection: SimpleListConnection<EntityEdge>?): Builder =
        this.apply {
      this.entityConnection = { entityConnection }
    }

    public fun build(): Query = Query(
      entity = entity,
      entityConnection = entityConnection,
    )
  }
}
