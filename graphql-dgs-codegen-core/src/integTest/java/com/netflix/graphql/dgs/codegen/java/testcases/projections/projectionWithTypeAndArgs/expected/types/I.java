package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class I {
  private String arg;

  public I() {
  }

  public I(String arg) {
    this.arg = arg;
  }

  public String getArg() {
    return arg;
  }

  public void setArg(String arg) {
    this.arg = arg;
  }

  @Override
  public String toString() {
    return "I{arg='" + arg + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    I that = (I) o;
    return Objects.equals(arg, that.arg);
  }

  @Override
  public int hashCode() {
    return Objects.hash(arg);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String arg;

    public I build() {
      I result = new I();
      result.arg = this.arg;
      return result;
    }

    public Builder arg(String arg) {
      this.arg = arg;
      return this;
    }
  }
}
