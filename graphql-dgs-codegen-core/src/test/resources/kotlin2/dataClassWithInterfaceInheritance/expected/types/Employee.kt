package kotlin2.dataClassWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename"
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Talent::class, name = "Talent")
])
public sealed interface Employee : Person {
  public override val firstname: String

  public override val lastname: String?

  public val company: String?
}
