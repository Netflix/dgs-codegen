package com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import com.netflix.graphql.dgs.codegen.cases.dataClassWithRecursiveField.expected.Generated
import java.lang.IllegalStateException
import java.util.Objects
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName

@Generated
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person(
  firstname: () -> String? = firstnameDefault,
  lastname: () -> String? = lastnameDefault,
  friends: () -> List<Person?>? = friendsDefault,
) {
  private val __firstname: () -> String? = firstname

  private val __lastname: () -> String? = lastname

  private val __friends: () -> List<Person?>? = friends

  @get:JvmName("getFirstname")
  public val firstname: String?
    get() = __firstname.invoke()

  @get:JvmName("getLastname")
  public val lastname: String?
    get() = __lastname.invoke()

  @get:JvmName("getFriends")
  public val friends: List<Person?>?
    get() = __friends.invoke()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Person) return false
    return (__firstname === firstnameDefault) == (other.__firstname === firstnameDefault) &&
        (__firstname === firstnameDefault || Objects.equals(firstname, other.firstname)) &&
    (__lastname === lastnameDefault) == (other.__lastname === lastnameDefault) && (__lastname ===
        lastnameDefault || Objects.equals(lastname, other.lastname)) &&
    (__friends === friendsDefault) == (other.__friends === friendsDefault) && (__friends ===
        friendsDefault || Objects.equals(friends, other.friends))
  }

  override fun hashCode(): Int = Objects.hash(if (__firstname === firstnameDefault) firstnameDefault
      else firstname,
  if (__lastname === lastnameDefault) lastnameDefault else lastname,
  if (__friends === friendsDefault) friendsDefault else friends)

  override fun toString(): String = listOfNotNull(if (__firstname === firstnameDefault) null else
      "firstname=" + firstname, if (__lastname === lastnameDefault) null else "lastname=" +
      lastname, if (__friends === friendsDefault) null else "friends=" +
      friends).joinToString(prefix = "Person(", postfix = ")")

  @Generated
  public companion object {
    private val firstnameDefault: () -> String? = 
        { throw IllegalStateException("Field `firstname` was not requested") }

    private val lastnameDefault: () -> String? = 
        { throw IllegalStateException("Field `lastname` was not requested") }

    private val friendsDefault: () -> List<Person?>? = 
        { throw IllegalStateException("Field `friends` was not requested") }
  }

  @Generated
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

    public fun build(): Person = Person(
      firstname = firstname,
      lastname = lastname,
      friends = friends,
    )
  }
}
