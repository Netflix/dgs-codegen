package kotlin2.union.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "__typename"
)
@JsonSubTypes(value = [
  JsonSubTypes.Type(value = Movie::class, name = "Movie"),
  JsonSubTypes.Type(value = Actor::class, name = "Actor")
])
public sealed interface SearchResult
