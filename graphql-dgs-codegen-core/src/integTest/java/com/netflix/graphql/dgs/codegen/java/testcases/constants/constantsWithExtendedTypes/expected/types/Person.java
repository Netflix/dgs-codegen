package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedTypes.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Person {
  private String firstname;

  private String lastname;

  private String email;

  public Person() {
  }

  public Person(String firstname, String lastname, String email) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "Person{firstname='" + firstname + "', lastname='" + lastname + "', email='" + email + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(firstname, that.firstname) &&
        Objects.equals(lastname, that.lastname) &&
        Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstname, lastname, email);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String firstname;

    private String lastname;

    private String email;

    public Person build() {
      Person result = new Person();
      result.firstname = this.firstname;
      result.lastname = this.lastname;
      result.email = this.email;
      return result;
    }

    public Builder firstname(String firstname) {
      this.firstname = firstname;
      return this;
    }

    public Builder lastname(String lastname) {
      this.lastname = lastname;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }
  }
}
