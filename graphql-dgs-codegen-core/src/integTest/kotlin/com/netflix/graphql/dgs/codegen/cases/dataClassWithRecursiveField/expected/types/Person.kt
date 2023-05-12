package com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  friends: () -> List<Person?>? = friendsDefault,
) {
  private val _firstname: () -> String? = firstname

  private val _lastname: () -> String? = lastname

  private val _friends: () -> List<Person?>? = friends

  @get:JvmName("getFirstname")
  public val firstname: String?
    get() = _firstname.invoke()

  @get:JvmName("getLastname")
  public val lastname: String?
    get() = _lastname.invoke()

  @get:JvmName("getFriends")
  public val friends: List<Person?>?
    get() = _friends.invoke()

  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }


    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }


    private val friendsDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `friends` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var firstname: () -> String? = firstnameDefault

    private var lastname: () -> String? = lastnameDefault

    private var friends: () -> List<Person?>? = friendsDefault

    @JsonProperty("firstname")
    public fun withFirstname(firstname: String?): Builder = this.apply {
      this.firstname = { firstname }
    }

    @JsonProperty("lastname")
    public fun withLastname(lastname: String?): Builder = this.apply {
      this.lastname = { lastname }
    }

    @JsonProperty("friends")
    public fun withFriends(friends: List<Person?>?): Builder = this.apply {
      this.friends = { friends }
    }

    public fun build() = Person(
      firstname = firstname,
      lastname = lastname,
      friends = friends,
    )
  }
}
