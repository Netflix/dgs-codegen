package com.netflix.graphql.dgs.codegen.cases.enumWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Query.Builder::class)
public class Query(
  types: () -> List<EmployeeTypes?>? = typesDefault,
) {
  private val __types: () -> List<EmployeeTypes?>? = types

  @get:JvmName("getTypes")
  public val types: List<EmployeeTypes?>?
    get() = __types.invoke()

  public companion object {
    private val typesDefault: () -> List<EmployeeTypes?>? = 
        { throw IllegalStateException("Field `types` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
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
