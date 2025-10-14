package com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

/**
 * Movies are fun to watch.
 * They also work well as examples in GraphQL.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = DgsMovie.Builder::class)
public class DgsMovie(
  title: () -> String? = titleDefault,
) {
  private val __title: () -> String? = title

  @get:JvmName("getTitle")
  public val title: String?
    get() = __title.invoke()

  public companion object {
    private val titleDefault: () -> String? = 
        { throw IllegalStateException("Field `title` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var title: () -> String? = titleDefault

    @JsonProperty("title")
    public fun withTitle(title: String?): Builder = this.apply {
      this.title = { title }
    }

    public fun build(): DgsMovie = DgsMovie(
      title = title,
    )
  }
}
