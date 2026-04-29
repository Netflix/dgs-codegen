package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Human implements SearchResult {
  private String id;

  private String name;

  private Integer totalCredits;

  public Human() {
  }

  public Human(String id, String name, Integer totalCredits) {
    this.id = id;
    this.name = name;
    this.totalCredits = totalCredits;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getTotalCredits() {
    return totalCredits;
  }

  public void setTotalCredits(Integer totalCredits) {
    this.totalCredits = totalCredits;
  }

  @Override
  public String toString() {
    return "Human{id='" + id + "', name='" + name + "', totalCredits='" + totalCredits + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Human that = (Human) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(totalCredits, that.totalCredits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, totalCredits);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String name;

    private Integer totalCredits;

    public Human build() {
      Human result = new Human();
      result.id = this.id;
      result.name = this.name;
      result.totalCredits = this.totalCredits;
      return result;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder totalCredits(Integer totalCredits) {
      this.totalCredits = totalCredits;
      return this;
    }
  }
}
