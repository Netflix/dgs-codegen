package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultStringValueForArray.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SomeType {
  private List<String> names = Arrays.asList("A", "B");

  public SomeType() {
  }

  public SomeType(List<String> names) {
    this.names = names;
  }

  public List<String> getNames() {
    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }

  @Override
  public String toString() {
    return "SomeType{names='" + names + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeType that = (SomeType) o;
    return Objects.equals(names, that.names);
  }

  @Override
  public int hashCode() {
    return Objects.hash(names);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private List<String> names = Arrays.asList("A", "B");

    public SomeType build() {
      SomeType result = new SomeType();
      result.names = this.names;
      return result;
    }

    public Builder names(List<String> names) {
      this.names = names;
      return this;
    }
  }
}
