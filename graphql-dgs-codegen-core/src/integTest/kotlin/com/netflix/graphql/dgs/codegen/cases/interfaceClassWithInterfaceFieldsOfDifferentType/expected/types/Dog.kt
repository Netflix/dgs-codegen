package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@Generated
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

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Dog) return false
    return (__name === nameDefault) == (other.__name === nameDefault) && (__name === nameDefault ||
        Objects.equals(name, other.name)) &&
    (__diet === dietDefault) == (other.__diet === dietDefault) && (__diet === dietDefault ||
        Objects.equals(diet, other.diet))
  }

  override fun hashCode(): Int = Objects.hash(if (__name === nameDefault) nameDefault else name,
  if (__diet === dietDefault) dietDefault else diet)

  override fun toString(): String = listOfNotNull(if (__name === nameDefault) null else "name=" +
      name, if (__diet === dietDefault) null else "diet=" + diet).joinToString(prefix = "Dog(",
      postfix = ")")

  @Generated
  public companion object {
    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val dietDefault: () -> Vegetarian? = 
        { throw IllegalStateException("Field `diet` was not requested") }
  }

  @Generated
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
