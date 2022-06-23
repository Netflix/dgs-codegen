package kotlin2.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  cars: () -> List<Car?>? = carsDefault
) {
  private val _cars: () -> List<Car?>? = cars

  public val cars: List<Car?>?
    get() = _cars.invoke()

  public companion object {
    private val carsDefault: () -> List<Car?>? = 
        { throw IllegalStateException("Field `cars` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var cars: () -> List<Car?>? = carsDefault

    @JsonProperty("cars")
    public fun withCars(cars: List<Car?>?): Builder = this.apply {
      this.cars = { cars }
    }

    public fun build() = Query(
      cars = cars,
    )
  }
}
