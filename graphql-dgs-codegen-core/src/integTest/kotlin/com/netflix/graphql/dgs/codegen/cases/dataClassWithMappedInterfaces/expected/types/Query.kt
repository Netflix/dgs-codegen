package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  products: () -> List<Product?>? = productsDefault,
) {
  private val __products: () -> List<Product?>? = products

  @get:JvmName("getProducts")
  public val products: List<Product?>?
    get() = __products.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__products === productsDefault) productsDefault else products,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Query &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__products === productsDefault) null else "products=" + products,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Query(", postfix =
      ")")

  @Generated
  public companion object {
    private val productsDefault: () -> List<Product?>? = 
        { throw IllegalStateException("Field `products` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var products: () -> List<Product?>? = productsDefault

    @JsonProperty("products")
    public fun withProducts(products: List<Product?>?): Builder = this.apply {
      this.products = { products }
    }

    public fun build(): Query = Query(
      products = products,
    )
  }
}
