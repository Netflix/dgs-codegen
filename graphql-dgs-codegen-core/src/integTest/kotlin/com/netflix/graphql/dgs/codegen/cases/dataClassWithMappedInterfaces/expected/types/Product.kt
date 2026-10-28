package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.Generated
import com.netflix.graphql.dgs.codegen.fixtures.Node
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Product.Builder::class)
public class Product(
  id: () -> String = idDefault,
) : Entity,
    Node {
  private val __id: () -> String = id

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getId")
  override val id: String
    get() = __id.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__id === idDefault) idDefault else id,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Product &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__id === idDefault) null else "id=" + id,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Product(", postfix =
      ")")

  @Generated
  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    public fun build(): Product = Product(
      id = id,
    )
  }
}
