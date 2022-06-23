package kotlin2.dataClassWIthNoFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Person.Builder::class)
public class Person() {
  public companion object

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    public fun build() = Person(

    )
  }
}
