package kotlin2.constantsWithExtendedQuery.expected.types

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
  people: () -> List<Person?>? = peopleDefault,
  friends: () -> List<Person?>? = friendsDefault,
) {
  private val _people: () -> List<Person?>? = people

  private val _friends: () -> List<Person?>? = friends

  public val people: List<Person?>?
    get() = _people.invoke()

  public val friends: List<Person?>?
    get() = _friends.invoke()

  public companion object {
    private val peopleDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `people` was not requested") }


    private val friendsDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `friends` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var people: () -> List<Person?>? = peopleDefault

    private var friends: () -> List<Person?>? = friendsDefault

    @JsonProperty("people")
    public fun withPeople(people: List<Person?>?): Builder = this.apply {
      this.people = { people }
    }

    @JsonProperty("friends")
    public fun withFriends(friends: List<Person?>?): Builder = this.apply {
      this.friends = { friends }
    }

    public fun build() = Query(
      people = people,
      friends = friends,
    )
  }
}
