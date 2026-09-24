package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Dog implements com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types.Pet {
  private String name;

  private Vegetarian diet;

  public Dog() {
  }

  public Dog(String name, Vegetarian diet) {
    this.name = name;
    this.diet = diet;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Vegetarian getDiet() {
    return diet;
  }

  public void setDiet(Vegetarian diet) {
    this.diet = diet;
  }

  @Override
  public String toString() {
    return "Dog{name='" + name + "', diet='" + diet + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dog that = (Dog) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(diet, that.diet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, diet);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name;

    private Vegetarian diet;

    public Dog build() {
      Dog result = new Dog();
      result.name = this.name;
      result.diet = this.diet;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder diet(Vegetarian diet) {
      this.diet = diet;
      return this;
    }
  }
}
