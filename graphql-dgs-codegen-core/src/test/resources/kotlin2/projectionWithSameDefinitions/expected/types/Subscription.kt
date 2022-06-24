package kotlin2.projectionWithSameDefinitions.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Subscription.Builder::class)
public class Subscription(
  shows: () -> List<Show?>? = showsDefault
) {
  private val _shows: () -> List<Show?>? = shows

  public val shows: List<Show?>?
    get() = _shows.invoke()

  public companion object {
    private val showsDefault: () -> List<Show?>? = 
        { throw IllegalStateException("Field `shows` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var shows: () -> List<Show?>? = showsDefault

    @JsonProperty("shows")
    public fun withShows(shows: List<Show?>?): Builder = this.apply {
      this.shows = { shows }
    }

    public fun build() = Subscription(
      shows = shows,
    )
  }
}
