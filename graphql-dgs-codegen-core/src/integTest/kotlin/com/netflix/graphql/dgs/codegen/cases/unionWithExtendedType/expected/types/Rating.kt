package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Int
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Rating.Builder::class)
public class Rating(
  stars: () -> Int? = starsDefault,
) : SearchResult {
  private val __stars: () -> Int? = stars

  @get:JvmName("getStars")
  public val stars: Int?
    get() = __stars.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val starsDefault: () -> Int? = 
        { throw IllegalStateException("Field `stars` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
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
