package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Talent implements com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected.types.Employee {
  private String firstname;

  private String lastname;

  private String company;

  private String imdbProfile;

  public Talent() {
  }

  public Talent(String firstname, String lastname, String company, String imdbProfile) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.company = company;
    this.imdbProfile = imdbProfile;
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

  public String getImdbProfile() {
    return imdbProfile;
  }

  public void setImdbProfile(String imdbProfile) {
    this.imdbProfile = imdbProfile;
  }

  @Override
  public String toString() {
    return "Talent{firstname='" + firstname + "', lastname='" + lastname + "', company='" + company + "', imdbProfile='" + imdbProfile + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Talent that = (Talent) o;
    return Objects.equals(firstname, that.firstname) &&
        Objects.equals(lastname, that.lastname) &&
        Objects.equals(company, that.company) &&
        Objects.equals(imdbProfile, that.imdbProfile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstname, lastname, company, imdbProfile);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String firstname;

    private String lastname;

    private String company;

    private String imdbProfile;

    public Talent build() {
      Talent result = new Talent();
      result.firstname = this.firstname;
      result.lastname = this.lastname;
      result.company = this.company;
      result.imdbProfile = this.imdbProfile;
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

    public Builder imdbProfile(String imdbProfile) {
      this.imdbProfile = imdbProfile;
      return this;
    }
  }
}
