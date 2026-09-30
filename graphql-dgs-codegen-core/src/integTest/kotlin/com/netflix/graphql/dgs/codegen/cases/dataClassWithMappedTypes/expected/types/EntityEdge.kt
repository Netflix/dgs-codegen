package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedTypes.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = EntityEdge.Builder::class)
public class EntityEdge(
  cursor: () -> String = cursorDefault,
  node: () -> Entity? = nodeDefault,
) {
  private val __cursor: () -> String = cursor

  private val __node: () -> Entity? = node

  @get:JvmName("getCursor")
  public val cursor: String
    get() = __cursor.invoke()

  @get:JvmName("getNode")
  public val node: Entity?
    get() = __node.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__cursor === cursorDefault) cursorDefault else cursor,
      if (__node === nodeDefault) nodeDefault else node,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is EntityEdge &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__cursor === cursorDefault) null else "cursor=" + cursor,
      if (__node === nodeDefault) null else "node=" + node,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "EntityEdge(", postfix
      = ")")

  @Generated
  public companion object {
    private val cursorDefault: () -> String = 
        { throw IllegalStateException("Field `cursor` was not requested") }

    private val nodeDefault: () -> Entity? = 
        { throw IllegalStateException("Field `node` was not requested") }
  }

  @Generated
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
      node = node,
    )
  }
}
