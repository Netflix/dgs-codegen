package com.netflix.graphql.dgs.codegen.cases.projectionWithPrimitives.expected.types

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
  string: () -> String? = stringDefault,
  strings: () -> List<String?>? = stringsDefault,
) {
  private val __string: () -> String? = string

  private val __strings: () -> List<String?>? = strings

  @get:JvmName("getString")
  public val string: String?
    get() = __string.invoke()

  @get:JvmName("getStrings")
  public val strings: List<String?>?
    get() = __strings.invoke()

  public companion object {
    private val stringDefault: () -> String? = 
        { throw IllegalStateException("Field `string` was not requested") }


    private val stringsDefault: () -> List<String?>? = 
        { throw IllegalStateException("Field `strings` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var string: () -> String? = stringDefault

    private var strings: () -> List<String?>? = stringsDefault

    @JsonProperty("string")
    public fun withString(string: String?): Builder = this.apply {
      this.string = { string }
    }

    @JsonProperty("strings")
    public fun withStrings(strings: List<String?>?): Builder = this.apply {
      this.strings = { strings }
    }

    public fun build(): Query = Query(
      string = string,
      strings = strings,
    )
  }
}
