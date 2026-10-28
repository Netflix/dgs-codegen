package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithNonNullableFields.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithNonNullableFields.expected.Generated;
import java.lang.String;

@Generated
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Employee.class, name = "Employee"))
public interface Person {
  String getFirstname();

  void setFirstname(String firstname);

  String getLastname();

  void setLastname(String lastname);
}
