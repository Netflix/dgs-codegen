package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.inputWithDefaultMap.expected.Generated
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  movies: () -> String? = moviesDefault,
) {
  private val __movies: () -> String? = movies

  @get:JvmName("getMovies")
  public val movies: String?
    get() = __movies.invoke()

  @Generated
  public companion object {
    private val moviesDefault: () -> String? = 
        { throw IllegalStateException("Field `movies` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var movies: () -> String? = moviesDefault

    @JsonProperty("movies")
    public fun withMovies(movies: String?): Builder = this.apply {
      this.movies = { movies }
    }

    public fun build(): Query = Query(
      movies = movies,
    )
  }
}
