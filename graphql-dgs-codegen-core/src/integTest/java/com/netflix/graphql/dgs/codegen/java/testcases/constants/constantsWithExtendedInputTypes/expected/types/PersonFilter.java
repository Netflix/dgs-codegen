package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedInputTypes.expected.types;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class PersonFilter {
  private String email;

  private Integer birthYear;

  public PersonFilter() {
  }

  public PersonFilter(String email, Integer birthYear) {
    this.email = email;
    this.birthYear = birthYear;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getBirthYear() {
    return birthYear;
  }

  public void setBirthYear(Integer birthYear) {
    this.birthYear = birthYear;
  }

  @Override
  public String toString() {
    return "PersonFilter{email='" + email + "', birthYear='" + birthYear + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PersonFilter that = (PersonFilter) o;
    return Objects.equals(email, that.email) &&
        Objects.equals(birthYear, that.birthYear);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, birthYear);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String email;

    private Integer birthYear;

    public PersonFilter build() {
      PersonFilter result = new PersonFilter();
      result.email = this.email;
      result.birthYear = this.birthYear;
      return result;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder birthYear(Integer birthYear) {
      this.birthYear = birthYear;
      return this;
    }
  }
}
