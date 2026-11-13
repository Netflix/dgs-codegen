package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Droid.Builder::class)
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

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__id === idDefault) idDefault else id,
      if (__name === nameDefault) nameDefault else name,
      if (__primaryFunction === primaryFunctionDefault) primaryFunctionDefault else primaryFunction,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Droid &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__id === idDefault) null else "id=" + id,
      if (__name === nameDefault) null else "name=" + name,
      if (__primaryFunction === primaryFunctionDefault) null else "primaryFunction=" +
      primaryFunction,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Droid(", postfix =
      ")")

  @Generated
  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }

    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val primaryFunctionDefault: () -> String? = 
        { throw IllegalStateException("Field `primaryFunction` was not requested") }
  }

  @Generated
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

    public fun build(): Droid = Droid(
      id = id,
      name = name,
      primaryFunction = primaryFunction,
    )
  }
}
