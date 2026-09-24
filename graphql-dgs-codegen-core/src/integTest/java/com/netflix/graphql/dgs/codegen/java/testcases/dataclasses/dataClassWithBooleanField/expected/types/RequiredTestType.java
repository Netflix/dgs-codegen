package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithBooleanField.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class RequiredTestType {
  private boolean isRequired;

  public RequiredTestType() {
  }

  public RequiredTestType(boolean isRequired) {
    this.isRequired = isRequired;
  }

  public boolean getIsRequired() {
    return isRequired;
  }

  public void setIsRequired(boolean isRequired) {
    this.isRequired = isRequired;
  }

  @Override
  public String toString() {
    return "RequiredTestType{isRequired='" + isRequired + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RequiredTestType that = (RequiredTestType) o;
    return isRequired == that.isRequired;
  }

  @Override
  public int hashCode() {
    return Objects.hash(isRequired);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private boolean isRequired;

    public RequiredTestType build() {
      RequiredTestType result = new RequiredTestType();
      result.isRequired = this.isRequired;
      return result;
    }

    public Builder isRequired(boolean isRequired) {
      this.isRequired = isRequired;
      return this;
    }
  }
}
