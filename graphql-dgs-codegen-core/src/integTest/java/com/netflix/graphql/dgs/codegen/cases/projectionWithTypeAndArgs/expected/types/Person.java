package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.String;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Employee.class, name = "Employee"))
public interface Person {
  String getFirstname();

  void setFirstname(String firstname);
}
