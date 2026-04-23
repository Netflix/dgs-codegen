package com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassWIthNoFields.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  me: () -> Person? = meDefault,
) {
  private val __me: () -> Person? = me

  @get:JvmName("getMe")
  public val me: Person?
    get() = __me.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val meDefault: () -> Person? = 
        { throw IllegalStateException("Field `me` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var me: () -> Person? = meDefault

    @JsonProperty("me")
    public fun withMe(me: Person?): Builder = this.apply {
      this.me = { me }
    }

    public fun build(): Query = Query(
      me = me,
    )
  }
}
