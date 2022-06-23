package kotlin2.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Double

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Performance.Builder::class)
public class Performance(
  zeroToSixty: () -> Double? = zeroToSixtyDefault,
  quarterMile: () -> Double? = quarterMileDefault
) {
  private val _zeroToSixty: () -> Double? = zeroToSixty

  private val _quarterMile: () -> Double? = quarterMile

  public val zeroToSixty: Double?
    get() = _zeroToSixty.invoke()

  public val quarterMile: Double?
    get() = _quarterMile.invoke()

  public companion object {
    private val zeroToSixtyDefault: () -> Double? = 
        { throw IllegalStateException("Field `zeroToSixty` was not requested") }


    private val quarterMileDefault: () -> Double? = 
        { throw IllegalStateException("Field `quarterMile` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var zeroToSixty: () -> Double? = zeroToSixtyDefault

    private var quarterMile: () -> Double? = quarterMileDefault

    @JsonProperty("zeroToSixty")
    public fun withZeroToSixty(zeroToSixty: Double?): Builder = this.apply {
      this.zeroToSixty = { zeroToSixty }
    }

    @JsonProperty("quarterMile")
    public fun withQuarterMile(quarterMile: Double?): Builder = this.apply {
      this.quarterMile = { quarterMile }
    }

    public fun build() = Performance(
      zeroToSixty = zeroToSixty,
      quarterMile = quarterMile,
    )
  }
}
