package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithNestedInputs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class I2 {
  private String arg1;

  private String arg2;

  public I2() {
  }

  public I2(String arg1, String arg2) {
    this.arg1 = arg1;
    this.arg2 = arg2;
  }

  public String getArg1() {
    return arg1;
  }

  public void setArg1(String arg1) {
    this.arg1 = arg1;
  }

  public String getArg2() {
    return arg2;
  }

  public void setArg2(String arg2) {
    this.arg2 = arg2;
  }

  @Override
  public String toString() {
    return "I2{arg1='" + arg1 + "', arg2='" + arg2 + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    I2 that = (I2) o;
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
    private String arg1;

    private String arg2;

    public I2 build() {
      I2 result = new I2();
      result.arg1 = this.arg1;
      result.arg2 = this.arg2;
      return result;
    }

    public Builder arg1(String arg1) {
      this.arg1 = arg1;
      return this;
    }

    public Builder arg2(String arg2) {
      this.arg2 = arg2;
      return this;
    }
  }
}
