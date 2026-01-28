package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
public class Query(
  movies: () -> List<String?>? = moviesDefault,
) {
  private val __movies: () -> List<String?>? = movies

  @get:JvmName("getMovies")
  public val movies: List<String?>?
    get() = __movies.invoke()

  public companion object {
    private val moviesDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `movies` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var movies: () -> List<String?>? = moviesDefault

    @JsonProperty("movies")
    public fun withMovies(movies: List<String?>?): Builder = this.apply {
      this.movies = { movies }
    }

    public fun build(): Query = Query(
      movies = movies,
    )
  }
}
