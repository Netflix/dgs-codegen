package com.netflix.graphql.dgs.codegen.cases.union.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Movie.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Movie.Builder::class)
public class Movie(
  title: () -> String? = titleDefault,
) : SearchResult {
  private val __title: () -> String? = title

  @get:JvmName("getTitle")
  public val title: String?
    get() = __title.invoke()

  public companion object {
    private val titleDefault: () -> String? = 
        { throw IllegalStateException("Field `title` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
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
