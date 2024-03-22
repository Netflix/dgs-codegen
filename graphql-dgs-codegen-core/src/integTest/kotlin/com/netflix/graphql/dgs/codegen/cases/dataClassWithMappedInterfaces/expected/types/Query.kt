package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
    products: () -> List<Product?>? = productsDefault
) {
    private val _products: () -> List<Product?>? = products

    @get:JvmName("getProducts")
    public val products: List<Product?>?
        get() = _products.invoke()

    public companion object {
        private val productsDefault: () -> List<Product?>? = { throw IllegalStateException("Field `products` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var products: () -> List<Product?>? = productsDefault

        @JsonProperty("products")
        public fun withProducts(products: List<Product?>?): Builder = this.apply {
            this.products = { products }
        }

        public fun build(): Query = Query(
            products = products
        )
    }
}
