package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.Generated
import java.lang.IllegalStateException
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = T.Builder::class)
public class T(
  _id: () -> String? = _idDefault,
  id: () -> String? = idDefault,
) : I {
  private val ___id: () -> String? = _id

  private val __id: () -> String? = id

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("get_id")
  override val _id: String?
    get() = ___id.invoke()

  @get:JvmName("getId")
  public val id: String?
    get() = __id.invoke()

  private fun `__$fieldValues`(): List<Any?> = listOf(
      if (___id === _idDefault) _idDefault else _id,
      if (__id === idDefault) idDefault else id,
  )

  override fun equals(other: Any?): Boolean = this === other || (other is T && `__$fieldValues`() ==
      other.`__$fieldValues`())

  override fun hashCode(): Int = `__$fieldValues`().hashCode()

  private fun `__$fieldStrings`(): List<String> = listOfNotNull(
      if (___id === _idDefault) null else "_id=" + _id,
      if (__id === idDefault) null else "id=" + id,
  )

  override fun toString(): String = `__$fieldStrings`().joinToString(prefix = "T(", postfix = ")")

  @Generated
  public companion object {
    private val _idDefault: () -> String? = 
        { throw IllegalStateException("Field `_id` was not requested") }

    private val idDefault: () -> String? = 
        { throw IllegalStateException("Field `id` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var _id: () -> String? = _idDefault

    private var id: () -> String? = idDefault

    @JsonProperty("_id")
    public fun with_id(_id: String?): Builder = this.apply {
      this._id = { _id }
    }

    @JsonProperty("id")
    public fun withId(id: String?): Builder = this.apply {
      this.id = { id }
    }

    public fun build(): T = T(
      _id = _id,
      id = id,
    )
  }
}
