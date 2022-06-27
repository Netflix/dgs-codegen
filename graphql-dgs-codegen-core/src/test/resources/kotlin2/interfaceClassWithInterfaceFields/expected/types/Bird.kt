package kotlin2.interfaceClassWithInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List

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
  private val _id: () -> String = id

  private val _name: () -> String? = name

  private val _address: () -> List<String> = address

  private val _mother: () -> Bird = mother

  private val _father: () -> Bird? = father

  private val _parents: () -> List<Bird?>? = parents

  public override val id: String
    get() = _id.invoke()

  public override val name: String?
    get() = _name.invoke()

  public override val address: List<String>
    get() = _address.invoke()

  public override val mother: Bird
    get() = _mother.invoke()

  public override val father: Bird?
    get() = _father.invoke()

  public override val parents: List<Bird?>?
    get() = _parents.invoke()

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

    public fun build() = Bird(
      id = id,
      name = name,
      address = address,
      mother = mother,
      father = father,
      parents = parents,
    )
  }
}
