package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Droid.Builder::class)
public class Droid(
  id: () -> String = idDefault,
  name: () -> String = nameDefault,
  primaryFunction: () -> String? = primaryFunctionDefault,
) : SearchResult {
  private val _id: () -> String = id

  private val _name: () -> String = name

  private val _primaryFunction: () -> String? = primaryFunction

  public val id: String
    get() = _id.invoke()

  public val name: String
    get() = _name.invoke()

  public val primaryFunction: String?
    get() = _primaryFunction.invoke()

  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }


    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }


    private val primaryFunctionDefault: () -> String? = 
        { throw IllegalStateException("Field `primaryFunction` was not requested") }

  }

  @JsonPOJOBuilder
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

    public fun build() = Droid(
      id = id,
      name = name,
      primaryFunction = primaryFunction,
    )
  }
}
