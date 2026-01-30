package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithReservedWord.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class SampleType {
  private String _return;

  public SampleType() {
  }

  public SampleType(String _return) {
    this._return = _return;
  }

  public String getReturn() {
    return _return;
  }

  public void setReturn(String _return) {
    this._return = _return;
  }

  @Override
  public String toString() {
    return "SampleType{return='" + _return + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SampleType that = (SampleType) o;
    return Objects.equals(_return, that._return);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_return);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String _return;

    public SampleType build() {
      SampleType result = new SampleType();
      result._return = this._return;
      return result;
    }

    public Builder _return(String _return) {
      this._return = _return;
      return this;
    }
  }
}
