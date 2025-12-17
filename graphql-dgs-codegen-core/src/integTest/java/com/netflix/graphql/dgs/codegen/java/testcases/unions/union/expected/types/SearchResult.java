package com.netflix.graphql.dgs.codegen.java.testcases.unions.union.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Movie.class, name = "Movie"),
    @JsonSubTypes.Type(value = Actor.class, name = "Actor")
})
public interface SearchResult {
}
