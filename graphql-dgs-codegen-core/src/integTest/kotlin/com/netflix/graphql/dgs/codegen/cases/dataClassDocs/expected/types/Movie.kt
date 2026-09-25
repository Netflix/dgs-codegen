package com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassDocs.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

/**
 * Movies are fun to watch.
 * They also work well as examples in GraphQL.
 */
@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Movie.Builder::class)
public class Movie(
  title: () -> String? = titleDefault,
) {
  private val __title: () -> String? = title

  @get:JvmName("getTitle")
  public val title: String?
    get() = __title.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__title === titleDefault) titleDefault else title,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Movie &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__title === titleDefault) null else "title=" + title,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Movie(", postfix =
      ")")

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
