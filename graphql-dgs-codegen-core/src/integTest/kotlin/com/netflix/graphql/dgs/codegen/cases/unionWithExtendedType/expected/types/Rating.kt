package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Rating.Builder::class)
public class Rating(
  stars: () -> Int? = starsDefault,
) : SearchResult {
  private val __stars: () -> Int? = stars

  @get:JvmName("getStars")
  public val stars: Int?
    get() = __stars.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__stars === starsDefault) starsDefault else stars,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Rating &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__stars === starsDefault) null else "stars=" + stars,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Rating(", postfix =
      ")")

  @Generated
  public companion object {
    private val starsDefault: () -> Int? = 
        { throw IllegalStateException("Field `stars` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var stars: () -> Int? = starsDefault

    @JsonProperty("stars")
    public fun withStars(stars: Int?): Builder = this.apply {
      this.stars = { stars }
    }

    public fun build(): Rating = Rating(
      stars = stars,
    )
  }
}
