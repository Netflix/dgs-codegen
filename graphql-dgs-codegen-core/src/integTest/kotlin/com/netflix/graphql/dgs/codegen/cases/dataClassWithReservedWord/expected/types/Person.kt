package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
public class Person(
  info: () -> String? = infoDefault,
  `interface`: () -> String? = interfaceDefault,
) {
  private val __info: () -> String? = info

  private val __interface: () -> String? = `interface`

  @get:JvmName("getInfo")
  public val info: String?
    get() = __info.invoke()

  @get:JvmName("getInterface")
  public val `interface`: String?
    get() = __interface.invoke()

  public companion object {
    private val infoDefault: () -> String? = 
        { throw IllegalStateException("Field `info` was not requested") }

    private val interfaceDefault: () -> String? = 
        { throw IllegalStateException("Field `interface` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var info: () -> String? = infoDefault

    private var `interface`: () -> String? = interfaceDefault

    @JsonProperty("info")
    public fun withInfo(info: String?): Builder = this.apply {
      this.info = { info }
    }

    @JsonProperty("interface")
    public fun withInterface(`interface`: String?): Builder = this.apply {
      this.`interface` = { `interface` }
    }

    public fun build(): Person = Person(
      info = info,
      `interface` = `interface`,
    )
  }
}
