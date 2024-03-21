package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = EntityEdge.Builder::class)
public class EntityEdge(
    cursor: () -> String = cursorDefault,
    node: () -> Entity? = nodeDefault
) {
    private val _cursor: () -> String = cursor

    private val _node: () -> Entity? = node

    @get:JvmName("getCursor")
    public val cursor: String
        get() = _cursor.invoke()

    @get:JvmName("getNode")
    public val node: Entity?
        get() = _node.invoke()

    public companion object {
        private val cursorDefault: () -> String = { throw IllegalStateException("Field `cursor` was not requested") }

        private val nodeDefault: () -> Entity? = { throw IllegalStateException("Field `node` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var cursor: () -> String = cursorDefault

        private var node: () -> Entity? = nodeDefault

        @JsonProperty("cursor")
        public fun withCursor(cursor: String): Builder = this.apply {
            this.cursor = { cursor }
        }

        @JsonProperty("node")
        public fun withNode(node: Entity?): Builder = this.apply {
            this.node = { node }
        }

        public fun build(): EntityEdge = EntityEdge(
            cursor = cursor,
            node = node
        )
    }
}
