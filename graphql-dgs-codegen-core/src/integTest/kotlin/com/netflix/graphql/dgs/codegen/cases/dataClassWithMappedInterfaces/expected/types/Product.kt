package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.fixtures.Node
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.Generated as ExpectedGenerated
import jakarta.`annotation`.Generated as AnnotationGenerated

@AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@ExpectedGenerated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Product.Builder::class)
public class Product(
  id: () -> String = idDefault,
) : Entity,
    Node {
  private val __id: () -> String = id

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getId")
  override val id: String
    get() = __id.invoke()

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }
  }

  @AnnotationGenerated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @ExpectedGenerated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    public fun build(): Product = Product(
      id = id,
    )
  }
}
