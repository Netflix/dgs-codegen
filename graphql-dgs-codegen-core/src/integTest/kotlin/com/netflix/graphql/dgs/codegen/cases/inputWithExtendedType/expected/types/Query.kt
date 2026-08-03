package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  movies: () -> List<String?>? = moviesDefault,
) {
  private val __movies: () -> List<String?>? = movies

  @get:JvmName("getMovies")
  public val movies: List<String?>?
    get() = __movies.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__movies === moviesDefault) == (other.__movies === moviesDefault) && (__movies ===
        moviesDefault || Objects.equals(movies, other.movies))
  }

  override fun hashCode(): Int = Objects.hash(if (__movies === moviesDefault) moviesDefault else
      movies)

  override fun toString(): String = listOfNotNull(if (__movies === moviesDefault) null else
      "movies=" + movies).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val moviesDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `movies` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
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
