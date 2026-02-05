package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithNonNullablePrimitive.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MyType {
  private int count;

  private boolean truth;

  private double floaty;

  public MyType() {
  }

  public MyType(int count, boolean truth, double floaty) {
    this.count = count;
    this.truth = truth;
    this.floaty = floaty;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public boolean getTruth() {
    return truth;
  }

  public void setTruth(boolean truth) {
    this.truth = truth;
  }

  public double getFloaty() {
    return floaty;
  }

  public void setFloaty(double floaty) {
    this.floaty = floaty;
  }

  @Override
  public String toString() {
    return "MyType{count='" + count + "', truth='" + truth + "', floaty='" + floaty + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MyType that = (MyType) o;
    return count == that.count &&
        truth == that.truth &&
        floaty == that.floaty;
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, truth, floaty);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private int count;

    private boolean truth;

    private double floaty;

    public MyType build() {
      MyType result = new MyType();
      result.count = this.count;
      result.truth = this.truth;
      result.floaty = this.floaty;
      return result;
    }

    public Builder count(int count) {
      this.count = count;
      return this;
    }

    public Builder truth(boolean truth) {
      this.truth = truth;
      return this;
    }

    public Builder floaty(double floaty) {
      this.floaty = floaty;
      return this;
    }
  }
}
