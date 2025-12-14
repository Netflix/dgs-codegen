package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Employee implements com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.types.Person {
  private String firstname;

  private String company;

  public Employee() {
  }

  public Employee(String firstname, String company) {
    this.firstname = firstname;
    this.company = company;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  @Override
  public String toString() {
    return "Employee{firstname='" + firstname + "', company='" + company + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Employee that = (Employee) o;
    return Objects.equals(firstname, that.firstname) &&
        Objects.equals(company, that.company);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstname, company);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String firstname;

    private String company;

    public Employee build() {
      Employee result = new Employee();
      result.firstname = this.firstname;
      result.company = this.company;
      return result;
    }

    public Builder firstname(String firstname) {
      this.firstname = firstname;
      return this;
    }

    public Builder company(String company) {
      this.company = company;
      return this;
    }
  }
}
