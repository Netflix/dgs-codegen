package kotlin2.`enum`.expected.types

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
  types: () -> List<EmployeeTypes?>? = typesDefault,
) {
  private val _types: () -> List<EmployeeTypes?>? = types

  public val types: List<EmployeeTypes?>?
    get() = _types.invoke()

  public companion object {
    private val typesDefault: () -> List<EmployeeTypes?>? = 
        { throw IllegalStateException("Field `types` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var types: () -> List<EmployeeTypes?>? = typesDefault

    @JsonProperty("types")
    public fun withTypes(types: List<EmployeeTypes?>?): Builder = this.apply {
      this.types = { types }
    }

    public fun build() = Query(
      types = types,
    )
  }
}
