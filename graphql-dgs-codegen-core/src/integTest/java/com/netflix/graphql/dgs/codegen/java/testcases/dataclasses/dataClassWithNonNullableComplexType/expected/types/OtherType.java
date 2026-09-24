package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithNonNullableComplexType.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class OtherType {
  private String name;

  public OtherType() {
  }

  public OtherType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "OtherType{name='" + name + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OtherType that = (OtherType) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;

    public OtherType build() {
      OtherType result = new OtherType();
      result.name = this.name;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }
  }
}
