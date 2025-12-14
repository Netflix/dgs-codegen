package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class I1 {
  private I1 arg1;

  private I2 arg2;

  public I1() {
  }

  public I1(I1 arg1, I2 arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  public I1 getArg1() {
    return arg1;
  }

  public void setArg1(I1 arg1) {
    this.arg1 = arg1;
  }

  public I2 getArg2() {
    return arg2;
  }

  public void setArg2(I2 arg2) {
    this.arg2 = arg2;
  }

  @Override
  public String toString() {
    return "I1{arg1='" + arg1 + "', arg2='" + arg2 + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    I1 that = (I1) o;
    return Objects.equals(arg1, that.arg1) &&
        Objects.equals(arg2, that.arg2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(arg1, arg2);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private I1 arg1;

    private I2 arg2;

    public I1 build() {
      I1 result = new I1();
      result.arg1 = this.arg1;
      result.arg2 = this.arg2;
      return result;
    }

    public Builder arg1(I1 arg1) {
      this.arg1 = arg1;
      return this;
    }

    public Builder arg2(I2 arg2) {
      this.arg2 = arg2;
      return this;
    }
  }
}
