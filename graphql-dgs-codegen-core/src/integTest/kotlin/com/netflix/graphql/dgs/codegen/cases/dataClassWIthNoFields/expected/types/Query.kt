package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  me: () -> Person? = meDefault,
) {
  private val __me: () -> Person? = me

  @get:JvmName("getMe")
  public val me: Person?
    get() = __me.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__me === meDefault) == (other.__me === meDefault) && (__me === meDefault ||
        Objects.equals(me, other.me))
  }

  override fun hashCode(): Int = Objects.hash(if (__me === meDefault) meDefault else me)

  @Generated
  public companion object {
    private val meDefault: () -> Person? = 
        { throw IllegalStateException("Field `me` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var me: () -> Person? = meDefault

    @JsonProperty("me")
    public fun withMe(me: Person?): Builder = this.apply {
      this.me = { me }
    }

    public fun build(): Query = Query(
      me = me,
    )
  }
}
