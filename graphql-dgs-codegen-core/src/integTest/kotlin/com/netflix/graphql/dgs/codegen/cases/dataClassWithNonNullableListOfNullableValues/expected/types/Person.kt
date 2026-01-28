package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableListOfNullableValues.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.String
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = Person.Builder::class)
public class Person(
  name: () -> String = nameDefault,
  email: () -> List<String?> = emailDefault,
) {
  private val __name: () -> String = name

  private val __email: () -> List<String?> = email

  @get:JvmName("getName")
  public val name: String
    get() = __name.invoke()

  @get:JvmName("getEmail")
  public val email: List<String?>
    get() = __email.invoke()

  public companion object {
    private val nameDefault: () -> String = 
        { throw IllegalStateException("Field `name` was not requested") }

    private val emailDefault: () -> List<String?> = 
        { throw IllegalStateException("Field `email` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var name: () -> String = nameDefault

    private var email: () -> List<String?> = emailDefault

    @JsonProperty("name")
    public fun withName(name: String): Builder = this.apply {
      this.name = { name }
    }

    @JsonProperty("email")
    public fun withEmail(email: List<String?>): Builder = this.apply {
      this.email = { email }
    }

    public fun build(): Person = Person(
      name = name,
      email = email,
    )
  }
}
