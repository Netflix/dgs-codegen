package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Engine.Builder::class)
public class Engine(
  type: () -> String? = typeDefault,
  bhp: () -> Int? = bhpDefault,
  size: () -> Double? = sizeDefault,
  performance: () -> Performance? = performanceDefault,
) {
  private val __type: () -> String? = type

  private val __bhp: () -> Int? = bhp

  private val __size: () -> Double? = size

  private val __performance: () -> Performance? = performance

  @get:JvmName("getType")
  public val type: String?
    get() = __type.invoke()

  @get:JvmName("getBhp")
  public val bhp: Int?
    get() = __bhp.invoke()

  @get:JvmName("getSize")
  public val size: Double?
    get() = __size.invoke()

  @get:JvmName("getPerformance")
  public val performance: Performance?
    get() = __performance.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Engine) return false
    return (__type === typeDefault) == (other.__type === typeDefault) && (__type === typeDefault ||
        Objects.equals(type, other.type)) &&
    (__bhp === bhpDefault) == (other.__bhp === bhpDefault) && (__bhp === bhpDefault ||
        Objects.equals(bhp, other.bhp)) &&
    (__size === sizeDefault) == (other.__size === sizeDefault) && (__size === sizeDefault ||
        Objects.equals(size, other.size)) &&
    (__performance === performanceDefault) == (other.__performance === performanceDefault) &&
        (__performance === performanceDefault || Objects.equals(performance, other.performance))
  }

  override fun hashCode(): Int = Objects.hash(if (__type === typeDefault) typeDefault else type,
  if (__bhp === bhpDefault) bhpDefault else bhp,
  if (__size === sizeDefault) sizeDefault else size,
  if (__performance === performanceDefault) performanceDefault else performance)

  override fun toString(): String = listOfNotNull(if (__type === typeDefault) null else "type=" +
      type, if (__bhp === bhpDefault) null else "bhp=" + bhp, if (__size === sizeDefault) null else
      "size=" + size, if (__performance === performanceDefault) null else "performance=" +
      performance).joinToString(prefix = "Engine(", postfix = ")")

  @Generated
  public companion object {
    private val typeDefault: () -> String? = 
        { throw IllegalStateException("Field `type` was not requested") }

    private val bhpDefault: () -> Int? = 
        { throw IllegalStateException("Field `bhp` was not requested") }

    private val sizeDefault: () -> Double? = 
        { throw IllegalStateException("Field `size` was not requested") }

    private val performanceDefault: () -> Performance? = 
        { throw IllegalStateException("Field `performance` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var type: () -> String? = typeDefault

    private var bhp: () -> Int? = bhpDefault

    private var size: () -> Double? = sizeDefault

    private var performance: () -> Performance? = performanceDefault

    @JsonProperty("type")
    public fun withType(type: String?): Builder = this.apply {
      this.type = { type }
    }

    @JsonProperty("bhp")
    public fun withBhp(bhp: Int?): Builder = this.apply {
      this.bhp = { bhp }
    }

    @JsonProperty("size")
    public fun withSize(size: Double?): Builder = this.apply {
      this.size = { size }
    }

    @JsonProperty("performance")
    public fun withPerformance(performance: Performance?): Builder = this.apply {
      this.performance = { performance }
    }

    public fun build(): Engine = Engine(
      type = type,
      bhp = bhp,
      size = size,
      performance = performance,
    )
  }
}
