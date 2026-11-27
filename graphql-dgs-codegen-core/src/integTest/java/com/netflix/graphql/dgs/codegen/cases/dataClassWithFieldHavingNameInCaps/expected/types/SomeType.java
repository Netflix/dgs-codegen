package com.netflix.graphql.dgs.codegen.cases.dataClassWithFieldHavingNameInCaps.expected.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class SomeType {
  @JsonProperty("CAPSField")
  private String CAPSField;

  public SomeType() {
  }

  public SomeType(String CAPSField) {
    this.CAPSField = CAPSField;
  }

  public String getCAPSField() {
    return CAPSField;
  }

  public void setCAPSField(String CAPSField) {
    this.CAPSField = CAPSField;
  }

  @Override
  public String toString() {
    return "SomeType{CAPSField='" + CAPSField + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeType that = (SomeType) o;
    return Objects.equals(CAPSField, that.CAPSField);
  }

  @Override
  public int hashCode() {
    return Objects.hash(CAPSField);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    @JsonProperty("CAPSField")
    private String CAPSField;

    public SomeType build() {
      SomeType result = new SomeType();
      result.CAPSField = this.CAPSField;
      return result;
    }

    public Builder CAPSField(String CAPSField) {
      this.CAPSField = CAPSField;
      return this;
    }
  }
}
