package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  cars: () -> List<Car?>? = carsDefault,
) {
  private val __cars: () -> List<Car?>? = cars

  @get:JvmName("getCars")
  public val cars: List<Car?>?
    get() = __cars.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val carsDefault: () -> List<Car?>? = 
        { throw IllegalStateException("Field `cars` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var cars: () -> List<Car?>? = carsDefault

    @JsonProperty("cars")
    public fun withCars(cars: List<Car?>?): Builder = this.apply {
      this.cars = { cars }
    }

    public fun build(): Query = Query(
      cars = cars,
    )
  }
}
