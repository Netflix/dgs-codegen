package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Human.class, name = "Human"),
    @JsonSubTypes.Type(value = Droid.class, name = "Droid")
})
public interface SearchResult {
}
