package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.types;

import com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.Generated;
import java.lang.String;

@Generated
public interface Person {
  String getFirstname();

  void setFirstname(String firstname);

  String getLastname();

  void setLastname(String lastname);
}
