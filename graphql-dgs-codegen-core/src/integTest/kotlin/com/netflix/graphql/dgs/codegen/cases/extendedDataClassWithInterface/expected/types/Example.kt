package com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.extendedDataClassWithInterface.expected.Generated
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
@JsonDeserialize(builder = Example.Builder::class)
public class Example(
  name: () -> String? = nameDefault,
  age: () -> Int? = ageDefault,
) : A,
    B {
  private val __name: () -> String? = name

  private val __age: () -> Int? = age

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getName")
  override val name: String?
    get() = __name.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getAge")
  override val age: Int?
    get() = __age.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Example) return false
    return (__name === nameDefault) == (other.__name === nameDefault) && (__name === nameDefault ||
        Objects.equals(name, other.name)) &&
    (__age === ageDefault) == (other.__age === ageDefault) && (__age === ageDefault ||
        Objects.equals(age, other.age))
  }

  override fun hashCode(): Int = Objects.hash(if (__name === nameDefault) nameDefault else name,
  if (__age === ageDefault) ageDefault else age)

  @Generated
  public companion object {
    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val ageDefault: () -> Int? = 
        { throw IllegalStateException("Field `age` was not requested") }
  }

  @Generated
  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String? = nameDefault

    private var age: () -> Int? = ageDefault

    @JsonProperty("name")
    public fun withName(name: String?): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("age")
    public fun withAge(age: Int?): Builder = this.apply {
      this.age = { age }
    }

    public fun build(): Example = Example(
      name = name,
      age = age,
    )
  }
}
