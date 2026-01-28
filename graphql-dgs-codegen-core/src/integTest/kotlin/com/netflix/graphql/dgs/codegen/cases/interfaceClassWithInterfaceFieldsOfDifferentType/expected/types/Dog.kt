package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types

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
@JsonDeserialize(builder = Dog.Builder::class)
public class Dog(
  name: () -> String? = nameDefault,
  diet: () -> Vegetarian? = dietDefault,
) : Pet {
  private val __name: () -> String? = name

  private val __diet: () -> Vegetarian? = diet

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getName")
  override val name: String?
    get() = __name.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getDiet")
  override val diet: Vegetarian?
    get() = __diet.invoke()

  public companion object {
    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val dietDefault: () -> Vegetarian? = 
        { throw IllegalStateException("Field `diet` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String? = nameDefault

    private var diet: () -> Vegetarian? = dietDefault

    @JsonProperty("name")
    public fun withName(name: String?): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("diet")
    public fun withDiet(diet: Vegetarian?): Builder = this.apply {
      this.diet = { diet }
    }

    public fun build(): Dog = Dog(
      name = name,
      diet = diet,
    )
  }
}
