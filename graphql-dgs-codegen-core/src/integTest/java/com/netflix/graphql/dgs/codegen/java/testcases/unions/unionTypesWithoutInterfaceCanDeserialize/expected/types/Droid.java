package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Droid implements SearchResult {
  private String id;

  private String name;

  private String primaryFunction;

  public Droid() {
  }

  public Droid(String id, String name, String primaryFunction) {
    this.id = id;
    this.name = name;
    this.primaryFunction = primaryFunction;
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

  public String getPrimaryFunction() {
    return primaryFunction;
  }

  public void setPrimaryFunction(String primaryFunction) {
    this.primaryFunction = primaryFunction;
  }

  @Override
  public String toString() {
    return "Droid{id='" + id + "', name='" + name + "', primaryFunction='" + primaryFunction + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Droid that = (Droid) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(primaryFunction, that.primaryFunction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, primaryFunction);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String name;

    private String primaryFunction;

    public Droid build() {
      Droid result = new Droid();
      result.id = this.id;
      result.name = this.name;
      result.primaryFunction = this.primaryFunction;
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

    public Builder primaryFunction(String primaryFunction) {
      this.primaryFunction = primaryFunction;
      return this;
    }
  }
}
