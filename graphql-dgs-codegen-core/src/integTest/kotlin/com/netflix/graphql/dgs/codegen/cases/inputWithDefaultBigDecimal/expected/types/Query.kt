package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultBigDecimal.expected.Generated
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  orders: () -> String? = ordersDefault,
) {
  private val __orders: () -> String? = orders

  @get:JvmName("getOrders")
  public val orders: String?
    get() = __orders.invoke()

  @Generated
  public companion object {
    private val ordersDefault: () -> String? = 
        { throw IllegalStateException("Field `orders` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var orders: () -> String? = ordersDefault

    @JsonProperty("orders")
    public fun withOrders(orders: String?): Builder = this.apply {
      this.orders = { orders }
    }

    public fun build(): Query = Query(
      orders = orders,
    )
  }
}
