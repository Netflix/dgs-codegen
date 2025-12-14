package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultIntValueForArray.expected.types;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SomeType {
  private List<Integer> numbers = Arrays.asList(1, 2, 3);

  public SomeType() {
  }

  public SomeType(List<Integer> numbers) {
    this.numbers = numbers;
  }

  public List<Integer> getNumbers() {
    return numbers;
  }

  public void setNumbers(List<Integer> numbers) {
    this.numbers = numbers;
  }

  @Override
  public String toString() {
    return "SomeType{numbers='" + numbers + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeType that = (SomeType) o;
    return Objects.equals(numbers, that.numbers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numbers);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private List<Integer> numbers = Arrays.asList(1, 2, 3);

    public SomeType build() {
      SomeType result = new SomeType();
      result.numbers = this.numbers;
      return result;
    }

    public Builder numbers(List<Integer> numbers) {
      this.numbers = numbers;
      return this;
    }
  }
}
