package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.extendedDataClassWithInterface.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Example implements com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.extendedDataClassWithInterface.expected.types.A, com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.extendedDataClassWithInterface.expected.types.B {
  private String name;

  private Integer age;

  public Example() {
  }

  public Example(String name, Integer age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "Example{name='" + name + "', age='" + age + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Example that = (Example) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(age, that.age);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;

    private Integer age;

    public Example build() {
      Example result = new Example();
      result.name = this.name;
      result.age = this.age;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder age(Integer age) {
      this.age = age;
      return this;
    }
  }
}
