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
@JsonDeserialize(builder = Dog.Builder::class)
public class Dog(
  id: () -> String = idDefault,
  name: () -> String? = nameDefault,
  address: () -> List<String> = addressDefault,
  mother: () -> Dog = motherDefault,
  father: () -> Dog? = fatherDefault,
  parents: () -> List<Dog?>? = parentsDefault,
) : Pet {
  private val __id: () -> String = id

  private val __name: () -> String? = name

  private val __address: () -> List<String> = address

  private val __mother: () -> Dog = mother

  private val __father: () -> Dog? = father

  private val __parents: () -> List<Dog?>? = parents

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
  override val mother: Dog
    get() = __mother.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getFather")
  override val father: Dog?
    get() = __father.invoke()

  @Suppress("INAPPLICABLE_JVM_NAME")
  @get:JvmName("getParents")
  override val parents: List<Dog?>?
    get() = __parents.invoke()

  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }

    private val nameDefault: () -> String? = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val addressDefault: () -> List<String> = 
        { throw IllegalStateException("Field `address` was not requested") }

    private val motherDefault: () -> Dog = 
        { throw IllegalStateException("Field `mother` was not requested") }

    private val fatherDefault: () -> Dog? = 
        { throw IllegalStateException("Field `father` was not requested") }

    private val parentsDefault: () -> List<Dog?>? = 
        { throw IllegalStateException("Field `parents` was not requested") }
  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    private var name: () -> String? = nameDefault

    private var address: () -> List<String> = addressDefault

    private var mother: () -> Dog = motherDefault

    private var father: () -> Dog? = fatherDefault

    private var parents: () -> List<Dog?>? = parentsDefault

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
    public fun withMother(mother: Dog): Builder = this.apply {
      this.mother = { mother }
    }

    @JsonProperty("father")
    public fun withFather(father: Dog?): Builder = this.apply {
      this.father = { father }
    }

    @JsonProperty("parents")
    public fun withParents(parents: List<Dog?>?): Builder = this.apply {
      this.parents = { parents }
    }

    public fun build(): Dog = Dog(
      id = id,
      name = name,
      address = address,
      mother = mother,
      father = father,
      parents = parents,
    )
  }
}
