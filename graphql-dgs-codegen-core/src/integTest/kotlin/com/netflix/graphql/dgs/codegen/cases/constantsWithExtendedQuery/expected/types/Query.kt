package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  people: () -> List<Person?>? = peopleDefault,
  friends: () -> List<Person?>? = friendsDefault,
) {
  private val __people: () -> List<Person?>? = people

  private val __friends: () -> List<Person?>? = friends

  @get:JvmName("getPeople")
  public val people: List<Person?>?
    get() = __people.invoke()

  @get:JvmName("getFriends")
  public val friends: List<Person?>?
    get() = __friends.invoke()

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

    public fun build(): Query = Query(
      people = people,
      friends = friends,
    )
  }
}
