package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  movies: () -> List<String?>? = moviesDefault,
) {
  private val _movies: () -> List<String?>? = movies

  @get:JvmName("getMovies")
  public val movies: List<String?>?
    get() = _movies.invoke()

  public companion object {
    private val moviesDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `movies` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var movies: () -> List<String?>? = moviesDefault

    @JsonProperty("movies")
    public fun withMovies(movies: List<String?>?): Builder = this.apply {
      this.movies = { movies }
    }

    public fun build() = Query(
      movies = movies,
    )
  }
}
