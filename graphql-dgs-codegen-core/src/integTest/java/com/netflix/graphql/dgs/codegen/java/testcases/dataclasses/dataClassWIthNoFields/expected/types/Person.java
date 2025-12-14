package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWIthNoFields.expected.types;

import java.lang.Override;
import java.lang.String;

public class Person {
  public Person() {
  }

  @Override
  public String toString() {
    return "Person{}";
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    public Person build() {
      Person result = new Person();
      return result;
    }
  }
}
