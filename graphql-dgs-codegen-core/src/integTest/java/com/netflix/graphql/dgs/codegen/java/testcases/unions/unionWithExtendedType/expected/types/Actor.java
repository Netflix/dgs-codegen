package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionWithExtendedType.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Actor implements SearchResult {
  private String name;

  public Actor() {
  }

  public Actor(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Actor{name='" + name + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Actor that = (Actor) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;

    public Actor build() {
      Actor result = new Actor();
      result.name = this.name;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }
  }
}
