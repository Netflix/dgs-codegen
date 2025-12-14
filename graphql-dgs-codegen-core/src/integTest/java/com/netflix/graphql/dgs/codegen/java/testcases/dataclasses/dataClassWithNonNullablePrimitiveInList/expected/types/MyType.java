package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithNonNullablePrimitiveInList.expected.types;

import java.lang.Boolean;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

public class MyType {
  private List<Integer> count;

  private List<Boolean> truth;

  private List<Double> floaty;

  public MyType() {
  }

  public MyType(List<Integer> count, List<Boolean> truth, List<Double> floaty) {
    this.count = count;
    this.truth = truth;
    this.floaty = floaty;
  }

  public List<Integer> getCount() {
    return count;
  }

  public void setCount(List<Integer> count) {
    this.count = count;
  }

  public List<Boolean> getTruth() {
    return truth;
  }

  public void setTruth(List<Boolean> truth) {
    this.truth = truth;
  }

  public List<Double> getFloaty() {
    return floaty;
  }

  public void setFloaty(List<Double> floaty) {
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
    return Objects.equals(count, that.count) &&
        Objects.equals(truth, that.truth) &&
        Objects.equals(floaty, that.floaty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, truth, floaty);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private List<Integer> count;

    private List<Boolean> truth;

    private List<Double> floaty;

    public MyType build() {
      MyType result = new MyType();
      result.count = this.count;
      result.truth = this.truth;
      result.floaty = this.floaty;
      return result;
    }

    public Builder count(List<Integer> count) {
      this.count = count;
      return this;
    }

    public Builder truth(List<Boolean> truth) {
      this.truth = truth;
      return this;
    }

    public Builder floaty(List<Double> floaty) {
      this.floaty = floaty;
      return this;
    }
  }
}
