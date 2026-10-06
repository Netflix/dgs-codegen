package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Actor.Builder::class)
public class Actor(
  name: () -> String? = nameDefault,
) : SearchResult {
  private val __name: () -> String? = name

  @get:JvmName("getName")
  public val name: String?
    get() = __name.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__name === nameDefault) nameDefault else name,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Actor &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__name === nameDefault) null else "name=" + name,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Actor(", postfix =
      ")")

  @Generated
  public companion object {
    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String? = nameDefault

    @JsonProperty("name")
    public fun withName(name: String?): Builder = this.apply {
      this.name = { name }
    }

    public fun build(): Actor = Actor(
      name = name,
    )
  }
}
