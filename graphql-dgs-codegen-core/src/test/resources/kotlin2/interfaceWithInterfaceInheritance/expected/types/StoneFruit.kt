package kotlin2.interfaceWithInterfaceInheritance.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.Boolean
import kotlin.collections.List

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename",
)
public sealed interface StoneFruit : Fruit {
  public override val seeds: List<Seed?>?

  public val fuzzy: Boolean?
}
