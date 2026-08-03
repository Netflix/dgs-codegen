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
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Car.Builder::class)
public class Car(
  make: () -> String? = makeDefault,
  model: () -> String? = modelDefault,
  engine: () -> Engine? = engineDefault,
) {
  private val __make: () -> String? = make

  private val __model: () -> String? = model

  private val __engine: () -> Engine? = engine

  @get:JvmName("getMake")
  public val make: String?
    get() = __make.invoke()

  @get:JvmName("getModel")
  public val model: String?
    get() = __model.invoke()

  @get:JvmName("getEngine")
  public val engine: Engine?
    get() = __engine.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Car) return false
    return (__make === makeDefault) == (other.__make === makeDefault) && (__make === makeDefault ||
        Objects.equals(make, other.make)) &&
    (__model === modelDefault) == (other.__model === modelDefault) && (__model === modelDefault ||
        Objects.equals(model, other.model)) &&
    (__engine === engineDefault) == (other.__engine === engineDefault) && (__engine ===
        engineDefault || Objects.equals(engine, other.engine))
  }

  override fun hashCode(): Int = Objects.hash(if (__make === makeDefault) makeDefault else make,
  if (__model === modelDefault) modelDefault else model,
  if (__engine === engineDefault) engineDefault else engine)

  override fun toString(): String = listOfNotNull(if (__make === makeDefault) null else "make=" +
      make, if (__model === modelDefault) null else "model=" + model, if (__engine ===
      engineDefault) null else "engine=" + engine).joinToString(prefix = "Car(", postfix = ")")

  @Generated
  public companion object {
    private val makeDefault: () -> String? = 
        { throw IllegalStateException("Field `make` was not requested") }

    private val modelDefault: () -> String? = 
        { throw IllegalStateException("Field `model` was not requested") }

    private val engineDefault: () -> Engine? = 
        { throw IllegalStateException("Field `engine` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var make: () -> String? = makeDefault

    private var model: () -> String? = modelDefault

    private var engine: () -> Engine? = engineDefault

    @JsonProperty("make")
    public fun withMake(make: String?): Builder = this.apply {
      this.make = { make }
    }

    @JsonProperty("model")
    public fun withModel(model: String?): Builder = this.apply {
      this.model = { model }
    }

    @JsonProperty("engine")
    public fun withEngine(engine: Engine?): Builder = this.apply {
      this.engine = { engine }
    }

    public fun build(): Car = Car(
      make = make,
      model = model,
      engine = engine,
    )
  }
}
