package kotlin2.projectionWithUnion.expected.types

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
  u: () -> U? = uDefault,
  us: () -> List<U?>? = usDefault
) {
  private val _u: () -> U? = u

  private val _us: () -> List<U?>? = us

  public val u: U?
    get() = _u.invoke()

  public val us: List<U?>?
    get() = _us.invoke()

  public companion object {
    private val uDefault: () -> U? = 
        { throw IllegalStateException("Field `u` was not requested") }


    private val usDefault: () -> List<U?>? = 
        { throw IllegalStateException("Field `us` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var u: () -> U? = uDefault

    private var us: () -> List<U?>? = usDefault

    @JsonProperty("u")
    public fun withU(u: U?): Builder = this.apply {
      this.u = { u }
    }

    @JsonProperty("us")
    public fun withUs(us: List<U?>?): Builder = this.apply {
      this.us = { us }
    }

    public fun build() = Query(
      u = u,
      us = us,
    )
  }
}
