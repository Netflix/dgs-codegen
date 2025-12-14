package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Vegetarian implements com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected.types.Diet {
  private String calories;

  private List<String> vegetables;

  public Vegetarian() {
  }

  public Vegetarian(String calories, List<String> vegetables) {
    this.calories = calories;
    this.vegetables = vegetables;
  }

  public String getCalories() {
    return calories;
  }

  public void setCalories(String calories) {
    this.calories = calories;
  }

  public List<String> getVegetables() {
    return vegetables;
  }

  public void setVegetables(List<String> vegetables) {
    this.vegetables = vegetables;
  }

  @Override
  public String toString() {
    return "Vegetarian{calories='" + calories + "', vegetables='" + vegetables + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Vegetarian that = (Vegetarian) o;
    return Objects.equals(calories, that.calories) &&
        Objects.equals(vegetables, that.vegetables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(calories, vegetables);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String calories;

    private List<String> vegetables;

    public Vegetarian build() {
      Vegetarian result = new Vegetarian();
      result.calories = this.calories;
      result.vegetables = this.vegetables;
      return result;
    }

    public Builder calories(String calories) {
      this.calories = calories;
      return this;
    }

    public Builder vegetables(List<String> vegetables) {
      this.vegetables = vegetables;
      return this;
    }
  }
}
