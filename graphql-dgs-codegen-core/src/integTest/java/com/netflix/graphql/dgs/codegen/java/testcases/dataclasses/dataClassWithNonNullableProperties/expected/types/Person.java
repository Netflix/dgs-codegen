package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithNonNullableProperties.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

public class Person {
  private String name;

  private List<String> email;

  public Person() {
  }

  public Person(String name, List<String> email) {
    this.name = name;
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getEmail() {
    return email;
  }

  public void setEmail(List<String> email) {
    this.email = email;
  }

  @Override
  public String toString() {
    return "Person{name='" + name + "', email='" + email + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;

    private List<String> email;

    public Person build() {
      Person result = new Person();
      result.name = this.name;
      result.email = this.email;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder email(List<String> email) {
      this.email = email;
      return this;
    }
  }
}
