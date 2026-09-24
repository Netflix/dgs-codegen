package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithExtendedInterfaceInheritance.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Employee implements com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithExtendedInterfaceInheritance.expected.types.Person {
  private String firstname;

  private String lastname;

  private String company;

  private int age;

  public Employee() {
  }

  public Employee(String firstname, String lastname, String company, int age) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.company = company;
    this.age = age;
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

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "Employee{firstname='" + firstname + "', lastname='" + lastname + "', company='" + company + "', age='" + age + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Employee that = (Employee) o;
    return Objects.equals(firstname, that.firstname) &&
        Objects.equals(lastname, that.lastname) &&
        Objects.equals(company, that.company) &&
        age == that.age;
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstname, lastname, company, age);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String firstname;

    private String lastname;

    private String company;

    private int age;

    public Employee build() {
      Employee result = new Employee();
      result.firstname = this.firstname;
      result.lastname = this.lastname;
      result.company = this.company;
      result.age = this.age;
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

    public Builder company(String company) {
      this.company = company;
      return this;
    }

    public Builder age(int age) {
      this.age = age;
      return this;
    }
  }
}
