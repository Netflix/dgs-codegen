package com.netflix.graphql.dgs.codegen.cases.interfaceWithUnderscoreFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

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

  public companion object {
    private val _idDefault: () -> String? = 
        { throw IllegalStateException("Field `_id` was not requested") }

    private val idDefault: () -> String? = 
        { throw IllegalStateException("Field `id` was not requested") }
  }

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
