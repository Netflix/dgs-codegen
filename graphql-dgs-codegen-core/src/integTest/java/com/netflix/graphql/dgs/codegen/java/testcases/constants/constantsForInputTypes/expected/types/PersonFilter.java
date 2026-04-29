package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsForInputTypes.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class PersonFilter {
  private String email;

  public PersonFilter() {
  }

  public PersonFilter(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "PersonFilter{email='" + email + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PersonFilter that = (PersonFilter) o;
    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String email;

    public PersonFilter build() {
      PersonFilter result = new PersonFilter();
      result.email = this.email;
      return result;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }
  }
}
