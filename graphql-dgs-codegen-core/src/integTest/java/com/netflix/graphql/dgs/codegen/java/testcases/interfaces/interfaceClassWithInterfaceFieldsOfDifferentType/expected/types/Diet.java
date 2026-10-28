package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.Generated;
import java.lang.String;

@Generated
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Vegetarian.class, name = "Vegetarian"))
public interface Diet {
  String getCalories();

  void setCalories(String calories);
}
