package kotlin2.projectionWithPrimitiveAndArgs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  string: () -> String? = stringDefault,
) {
  private val _string: () -> String? = string

  public val string: String?
    get() = _string.invoke()

  public companion object {
    private val stringDefault: () -> String? = 
        { throw IllegalStateException("Field `string` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var string: () -> String? = stringDefault

    @JsonProperty("string")
    public fun withString(string: String?): Builder = this.apply {
      this.string = { string }
    }

    public fun build() = Query(
      string = string,
    )
  }
}
