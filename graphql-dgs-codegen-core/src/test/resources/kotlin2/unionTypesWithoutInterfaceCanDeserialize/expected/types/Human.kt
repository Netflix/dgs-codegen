package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Int
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Human.Builder::class)
public class Human(
  id: () -> String = idDefault,
  name: () -> String = nameDefault,
  totalCredits: () -> Int? = totalCreditsDefault
) : SearchResult {
  private val _id: () -> String = id

  private val _name: () -> String = name

  private val _totalCredits: () -> Int? = totalCredits

  public val id: String
    get() = _id.invoke()

  public val name: String
    get() = _name.invoke()

  public val totalCredits: Int?
    get() = _totalCredits.invoke()

  public companion object {
    private val idDefault: () -> String = 
        { throw IllegalStateException("Field `id` was not requested") }


    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }


    private val totalCreditsDefault: () -> Int? = 
        { throw IllegalStateException("Field `totalCredits` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var id: () -> String = idDefault

    private var name: () -> String = nameDefault

    private var totalCredits: () -> Int? = totalCreditsDefault

    @JsonProperty("id")
    public fun withId(id: String): Builder = this.apply {
      this.id = { id }
    }

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("totalCredits")
    public fun withTotalCredits(totalCredits: Int?): Builder = this.apply {
      this.totalCredits = { totalCredits }
    }

    public fun build() = Human(
      id = id,
      name = name,
      totalCredits = totalCredits,
    )
  }
}
