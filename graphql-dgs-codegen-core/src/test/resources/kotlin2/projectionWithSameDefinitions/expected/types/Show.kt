package kotlin2.projectionWithSameDefinitions.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Int
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Show.Builder::class)
public class Show(
  id: () -> Int? = idDefault,
  title: () -> String? = titleDefault
) {
  private val _id: () -> Int? = id

  private val _title: () -> String? = title

  public val id: Int?
    get() = _id.invoke()

  public val title: String?
    get() = _title.invoke()

  public companion object {
    private val idDefault: () -> Int? = 
        { throw IllegalStateException("Field `id` was not requested") }


    private val titleDefault: () -> String? = 
        { throw IllegalStateException("Field `title` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> Int? = idDefault

    private var title: () -> String? = titleDefault

    @JsonProperty("id")
    public fun withId(id: Int?): Builder = this.apply {
      this.id = { id }
    }

    @JsonProperty("title")
    public fun withTitle(title: String?): Builder = this.apply {
      this.title = { title }
    }

    public fun build() = Show(
      id = id,
      title = title,
    )
  }
}
