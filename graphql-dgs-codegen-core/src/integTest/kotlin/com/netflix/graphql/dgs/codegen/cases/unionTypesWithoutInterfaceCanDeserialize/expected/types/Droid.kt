package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.types

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
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Droid.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Droid.Builder::class)
public class Droid(
  id: () -> String = idDefault,
  name: () -> String = nameDefault,
  primaryFunction: () -> String? = primaryFunctionDefault,
) : SearchResult {
  private val __id: () -> String = id

  private val __name: () -> String = name

  private val __primaryFunction: () -> String? = primaryFunction

  @get:JvmName("getId")
  public val id: String
    get() = __id.invoke()

  @get:JvmName("getName")
  public val name: String
    get() = __name.invoke()

  @get:JvmName("getPrimaryFunction")
  public val primaryFunction: String?
    get() = __primaryFunction.invoke()

  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }

    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val primaryFunctionDefault: () -> String? = 
        { throw IllegalStateException("Field `primaryFunction` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    private var name: () -> String = nameDefault

    private var primaryFunction: () -> String? = primaryFunctionDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("primaryFunction")
    public fun withPrimaryFunction(primaryFunction: String?): Builder = this.apply {
      this.primaryFunction = { primaryFunction }
    }

    public fun build(): Droid = Droid(
      id = id,
      name = name,
      primaryFunction = primaryFunction,
    )
  }
}
