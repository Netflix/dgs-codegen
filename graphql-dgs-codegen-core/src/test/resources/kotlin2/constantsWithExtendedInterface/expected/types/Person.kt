package kotlin2.constantsWithExtendedInterface.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.Int
import kotlin.String

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface Person {
  public val firstname: String

  public val lastname: String?

  public val age: Int?
}
