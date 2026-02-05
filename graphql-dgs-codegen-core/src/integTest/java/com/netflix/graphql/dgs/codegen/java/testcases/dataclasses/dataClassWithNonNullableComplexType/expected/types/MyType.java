package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithNonNullableComplexType.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MyType {
  private OtherType other;

  public MyType() {
  }

  public MyType(OtherType other) {
    this.other = other;
  }

  public OtherType getOther() {
    return other;
  }

  public void setOther(OtherType other) {
    this.other = other;
  }

  @Override
  public String toString() {
    return "MyType{other='" + other + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MyType that = (MyType) o;
    return Objects.equals(other, that.other);
  }

  @Override
  public int hashCode() {
    return Objects.hash(other);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private OtherType other;

    public MyType build() {
      MyType result = new MyType();
      result.other = this.other;
      return result;
    }

    public Builder other(OtherType other) {
      this.other = other;
      return this;
    }
  }
}
