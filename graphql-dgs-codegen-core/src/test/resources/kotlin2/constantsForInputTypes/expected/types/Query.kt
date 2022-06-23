package kotlin2.constantsForInputTypes.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  people: () -> List<Person?>? = peopleDefault
) {
  private val _people: () -> List<Person?>? = people

  public val people: List<Person?>?
    get() = _people.invoke()

  public companion object {
    private val peopleDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `people` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var people: () -> List<Person?>? = peopleDefault

    @JsonProperty("people")
    public fun withPeople(people: List<Person?>?): Builder = this.apply {
      this.people = { people }
    }

    public fun build() = Query(
      people = people,
    )
  }
}
