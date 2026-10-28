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
@JsonDeserialize(builder = Human.Builder::class)
public class Human(
  id: () -> String = idDefault,
  name: () -> String = nameDefault,
  totalCredits: () -> Int? = totalCreditsDefault,
) : SearchResult {
  private val __id: () -> String = id

  private val __name: () -> String = name

  private val __totalCredits: () -> Int? = totalCredits

  @get:JvmName("getId")
  public val id: String
    get() = __id.invoke()

  @get:JvmName("getName")
  public val name: String
    get() = __name.invoke()

  @get:JvmName("getTotalCredits")
  public val totalCredits: Int?
    get() = __totalCredits.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (__id === idDefault) idDefault else id,
      if (__name === nameDefault) nameDefault else name,
      if (__totalCredits === totalCreditsDefault) totalCreditsDefault else totalCredits,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is Human &&
      `__$fieldValues`() == other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (__id === idDefault) null else "id=" + id,
      if (__name === nameDefault) null else "name=" + name,
      if (__totalCredits === totalCreditsDefault) null else "totalCredits=" + totalCredits,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "Human(", postfix =
      ")")

  @Generated
  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }

    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val totalCreditsDefault: () -> Int? = 
        { throw IllegalStateException("Field `totalCredits` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    private var name: () -> String = nameDefault

    private var totalCredits: () -> Int? = totalCreditsDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("totalCredits")
    public fun withTotalCredits(totalCredits: Int?): Builder = this.apply {
      this.totalCredits = { totalCredits }
    }

    public fun build(): Human = Human(
      id = id,
      name = name,
      totalCredits = totalCredits,
    )
  }
}
