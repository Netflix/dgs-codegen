package com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  types: () -> List<EmployeeTypes?>? = typesDefault,
) {
  private val __types: () -> List<EmployeeTypes?>? = types

  @get:JvmName("getTypes")
  public val types: List<EmployeeTypes?>?
    get() = __types.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Query) return false
    return (__types === typesDefault) == (other.__types === typesDefault) && (__types ===
        typesDefault || Objects.equals(types, other.types))
  }

  override fun hashCode(): Int = Objects.hash(if (__types === typesDefault) typesDefault else types)

  override fun toString(): String = listOfNotNull(if (__types === typesDefault) null else "types=" +
      types).joinToString(prefix = "Query(", postfix = ")")

  @Generated
  public companion object {
    private val typesDefault: () -> List<EmployeeTypes?>? = 
        { throw IllegalStateException("Field `types` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var types: () -> List<EmployeeTypes?>? = typesDefault

    @JsonProperty("types")
    public fun withTypes(types: List<EmployeeTypes?>?): Builder = this.apply {
      this.types = { types }
    }

    public fun build(): Query = Query(
      types = types,
    )
  }
}
