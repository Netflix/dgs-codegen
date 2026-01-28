package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Bird.Builder::class)
public class Bird(
  id: () -> String = idDefault,
  name: () -> String? = nameDefault,
  address: () -> List<String> = addressDefault,
  mother: () -> Bird = motherDefault,
  father: () -> Bird? = fatherDefault,
  parents: () -> List<Bird?>? = parentsDefault,
) : Pet {
  private val __id: () -> String = id

  private val __name: () -> String? = name

  private val __address: () -> List<String> = address

  private val __mother: () -> Bird = mother

  private val __father: () -> Bird? = father

  private val __parents: () -> List<Bird?>? = parents

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getId")
  override val id: String
    get() = __id.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getName")
  override val name: String?
    get() = __name.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getAddress")
  override val address: List<String>
    get() = __address.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getMother")
  override val mother: Bird
    get() = __mother.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFather")
  override val father: Bird?
    get() = __father.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getParents")
  override val parents: List<Bird?>?
    get() = __parents.invoke()

  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }

    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val addressDefault: () -> List<String> = 
        { throw IllegalStateException("Field `address` was not requested") }

    private val motherDefault: () -> Bird = 
        { throw IllegalStateException("Field `mother` was not requested") }

    private val fatherDefault: () -> Bird? = 
        { throw IllegalStateException("Field `father` was not requested") }

    private val parentsDefault: () -> List<Bird?>? = 
        { throw IllegalStateException("Field `parents` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    private var name: () -> String? = nameDefault

    private var address: () -> List<String> = addressDefault

    private var mother: () -> Bird = motherDefault

    private var father: () -> Bird? = fatherDefault

    private var parents: () -> List<Bird?>? = parentsDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    @JsonProperty("name")
    public fun withName(name: String?): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("address")
    public fun withAddress(address: List<String>): Builder = this.apply {
      this.address = { address }
    }

    @JsonProperty("mother")
    public fun withMother(mother: Bird): Builder = this.apply {
      this.mother = { mother }
    }

    @JsonProperty("father")
    public fun withFather(father: Bird?): Builder = this.apply {
      this.father = { father }
    }

    @JsonProperty("parents")
    public fun withParents(parents: List<Bird?>?): Builder = this.apply {
      this.parents = { parents }
    }

    public fun build(): Bird = Bird(
      id = id,
      name = name,
      address = address,
      mother = mother,
      father = father,
      parents = parents,
    )
  }
}
