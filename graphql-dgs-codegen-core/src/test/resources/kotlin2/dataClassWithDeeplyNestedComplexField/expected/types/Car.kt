package kotlin2.dataClassWithDeeplyNestedComplexField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Car.Builder::class)
public class Car(
  make: () -> String? = makeDefault,
  model: () -> String? = modelDefault,
  engine: () -> Engine? = engineDefault
) {
  private val _make: () -> String? = make

  private val _model: () -> String? = model

  private val _engine: () -> Engine? = engine

  public val make: String?
    get() = _make.invoke()

  public val model: String?
    get() = _model.invoke()

  public val engine: Engine?
    get() = _engine.invoke()

  public companion object {
    private val makeDefault: () -> String? = 
        { throw IllegalStateException("Field `make` was not requested") }


    private val modelDefault: () -> String? = 
        { throw IllegalStateException("Field `model` was not requested") }


    private val engineDefault: () -> Engine? = 
        { throw IllegalStateException("Field `engine` was not requested") }

  }

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

    public fun build() = Car(
      make = make,
      model = model,
      engine = engine,
    )
  }
}
