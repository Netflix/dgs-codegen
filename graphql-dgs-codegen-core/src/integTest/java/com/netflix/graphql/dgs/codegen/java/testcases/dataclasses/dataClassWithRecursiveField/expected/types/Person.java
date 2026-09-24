package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithRecursiveField.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

public class Person {
  private String firstname;

  private String lastname;

  private List<Person> friends;

  public Person() {
  }

  public Person(String firstname, String lastname, List<Person> friends) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.friends = friends;
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

  public List<Person> getFriends() {
    return friends;
  }

  public void setFriends(List<Person> friends) {
    this.friends = friends;
  }

  @Override
  public String toString() {
    return "Person{firstname='" + firstname + "', lastname='" + lastname + "', friends='" + friends + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(firstname, that.firstname) &&
        Objects.equals(lastname, that.lastname) &&
        Objects.equals(friends, that.friends);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstname, lastname, friends);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String firstname;

    private String lastname;

    private List<Person> friends;

    public Person build() {
      Person result = new Person();
      result.firstname = this.firstname;
      result.lastname = this.lastname;
      result.friends = this.friends;
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

    public Builder friends(List<Person> friends) {
      this.friends = friends;
      return this;
    }
  }
}
