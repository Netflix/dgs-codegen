package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import graphql.relay.SimpleListConnection
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
    entity: () -> List<Entity?>? = entityDefault,
    entityConnection: () -> SimpleListConnection<EntityEdge>? = entityConnectionDefault
) {
    private val _entity: () -> List<Entity?>? = entity

    private val _entityConnection: () -> SimpleListConnection<EntityEdge>? = entityConnection

    @get:JvmName("getEntity")
    public val entity: List<Entity?>?
        get() = _entity.invoke()

    @get:JvmName("getEntityConnection")
    public val entityConnection: SimpleListConnection<EntityEdge>?
        get() = _entityConnection.invoke()

    public companion object {
        private val entityDefault: () -> List<Entity?>? = { throw IllegalStateException("Field `entity` was not requested") }

        private val entityConnectionDefault: () -> SimpleListConnection<EntityEdge>? = { throw IllegalStateException("Field `entityConnection` was not requested") }
    }

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
            entityConnection = entityConnection
        )
    }
}
