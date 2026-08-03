package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__products === productsDefault) == (other.__products === productsDefault) && (__products
        === productsDefault || Objects.equals(products, other.products))
  }

  override fun hashCode(): Int = Objects.hash(if (__products === productsDefault) productsDefault
      else products)

  override fun toString(): String = listOfNotNull(if (__products === productsDefault) null else
      "products=" + products).joinToString(prefix = "Query(", postfix = ")")

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
