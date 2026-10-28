package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.extendedDataClassWithInterface.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.extendedDataClassWithInterface.expected.Generated;
import java.lang.String;

@Generated
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Example.class, name = "Example"))
public interface A {
  String getName();

  void setName(String name);
}
