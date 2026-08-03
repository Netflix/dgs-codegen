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
@JsonDeserialize(builder = Actor.Builder::class)
public class Actor(
  name: () -> String? = nameDefault,
) : SearchResult {
  private val __name: () -> String? = name

  @get:JvmName("getName")
  public val name: String?
    get() = __name.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Actor) return false
    return (__name === nameDefault) == (other.__name === nameDefault) && (__name === nameDefault ||
        Objects.equals(name, other.name))
  }

  override fun hashCode(): Int = Objects.hash(if (__name === nameDefault) nameDefault else name)

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
