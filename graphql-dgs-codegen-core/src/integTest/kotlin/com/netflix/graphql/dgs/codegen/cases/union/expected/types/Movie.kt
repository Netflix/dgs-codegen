package com.netflix.graphql.dgs.codegen.cases.union.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.union.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Movie.Builder::class)
public class Movie(
  title: () -> String? = titleDefault,
) : SearchResult {
  private val __title: () -> String? = title

  @get:JvmName("getTitle")
  public val title: String?
    get() = __title.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Movie) return false
    return (__title === titleDefault) == (other.__title === titleDefault) && (__title ===
        titleDefault || Objects.equals(title, other.title))
  }

  override fun hashCode(): Int = Objects.hash(if (__title === titleDefault) titleDefault else title)

  @Generated
  public companion object {
    private val titleDefault: () -> String? = 
        { throw IllegalStateException("Field `title` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var title: () -> String? = titleDefault

    @JsonProperty("title")
    public fun withTitle(title: String?): Builder = this.apply {
      this.title = { title }
    }

    public fun build(): Movie = Movie(
      title = title,
    )
  }
}
