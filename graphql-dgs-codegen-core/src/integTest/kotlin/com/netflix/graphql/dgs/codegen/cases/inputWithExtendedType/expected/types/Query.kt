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
import com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  movies: () -> List<String?>? = moviesDefault,
) {
  private val __movies: () -> List<String?>? = movies

  @get:JvmName("getMovies")
  public val movies: List<String?>?
    get() = __movies.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val moviesDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `movies` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
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
