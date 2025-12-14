package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithExtendedInterface.expected.types;

import java.lang.Integer;
import java.lang.String;

public interface Person {
  String getFirstname();

  void setFirstname(String firstname);

  String getLastname();

  void setLastname(String lastname);

  Integer getAge();

  void setAge(Integer age);
}
