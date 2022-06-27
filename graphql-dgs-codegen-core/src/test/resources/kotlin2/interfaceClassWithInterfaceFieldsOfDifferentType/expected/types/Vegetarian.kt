package kotlin2.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Vegetarian.Builder::class)
public class Vegetarian(
  calories: () -> String? = caloriesDefault,
  vegetables: () -> List<String?>? = vegetablesDefault,
) : Diet {
  private val _calories: () -> String? = calories

  private val _vegetables: () -> List<String?>? = vegetables

  public override val calories: String?
    get() = _calories.invoke()

  public val vegetables: List<String?>?
    get() = _vegetables.invoke()

  public companion object {
    private val caloriesDefault: () -> String? = 
        { throw IllegalStateException("Field `calories` was not requested") }


    private val vegetablesDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `vegetables` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var calories: () -> String? = caloriesDefault

    private var vegetables: () -> List<String?>? = vegetablesDefault

    @JsonProperty("calories")
    public fun withCalories(calories: String?): Builder = this.apply {
      this.calories = { calories }
    }

    @JsonProperty("vegetables")
    public fun withVegetables(vegetables: List<String?>?): Builder = this.apply {
      this.vegetables = { vegetables }
    }

    public fun build() = Vegetarian(
      calories = calories,
      vegetables = vegetables,
    )
  }
}
