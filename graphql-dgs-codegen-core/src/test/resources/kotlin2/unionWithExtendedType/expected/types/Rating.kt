package kotlin2.unionWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Int

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Rating.Builder::class)
public class Rating(
  stars: () -> Int? = starsDefault,
) : SearchResult {
  private val _stars: () -> Int? = stars

  public val stars: Int?
    get() = _stars.invoke()

  public companion object {
    private val starsDefault: () -> Int? = 
        { throw IllegalStateException("Field `stars` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var stars: () -> Int? = starsDefault

    @JsonProperty("stars")
    public fun withStars(stars: Int?): Builder = this.apply {
      this.stars = { stars }
    }

    public fun build() = Rating(
      stars = stars,
    )
  }
}
