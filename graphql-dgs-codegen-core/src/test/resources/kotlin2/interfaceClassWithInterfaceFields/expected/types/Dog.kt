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
@JsonDeserialize(builder = Dog.Builder::class)
public class Dog(
  id: () -> String = idDefault,
  name: () -> String? = nameDefault,
  address: () -> List<String> = addressDefault,
  mother: () -> Dog = motherDefault,
  father: () -> Dog? = fatherDefault,
  parents: () -> List<Dog?>? = parentsDefault,
) : Pet {
  private val _id: () -> String = id

  private val _name: () -> String? = name

  private val _address: () -> List<String> = address

  private val _mother: () -> Dog = mother

  private val _father: () -> Dog? = father

  private val _parents: () -> List<Dog?>? = parents

  public override val id: String
    get() = _id.invoke()

  public override val name: String?
    get() = _name.invoke()

  public override val address: List<String>
    get() = _address.invoke()

  public override val mother: Dog
    get() = _mother.invoke()

  public override val father: Dog?
    get() = _father.invoke()

  public override val parents: List<Dog?>?
    get() = _parents.invoke()

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

    public fun build() = Dog(
      id = id,
      name = name,
      address = address,
      mother = mother,
      father = father,
      parents = parents,
    )
  }
}
