package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Person) return false
    return (__info === infoDefault) == (other.__info === infoDefault) && (__info === infoDefault ||
        Objects.equals(info, other.info)) &&
    (__interface === interfaceDefault) == (other.__interface === interfaceDefault) && (__interface
        === interfaceDefault || Objects.equals(`interface`, other.`interface`))
  }

  override fun hashCode(): Int = Objects.hash(if (__info === infoDefault) infoDefault else info,
  if (__interface === interfaceDefault) interfaceDefault else `interface`)

  @Generated
  public companion object {
    private val infoDefault: () -> String? = 
        { throw IllegalStateException("Field `info` was not requested") }

    private val interfaceDefault: () -> String? = 
        { throw IllegalStateException("Field `interface` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
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
