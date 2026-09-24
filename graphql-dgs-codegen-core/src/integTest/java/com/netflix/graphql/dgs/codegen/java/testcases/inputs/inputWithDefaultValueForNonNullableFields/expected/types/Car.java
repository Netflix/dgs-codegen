package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForNonNullableFields.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Car {
  private String brand = "BMW";

  public Car() {
  }

  public Car(String brand) {
    this.brand = brand;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  @Override
  public String toString() {
    return "Car{brand='" + brand + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Car that = (Car) o;
    return Objects.equals(brand, that.brand);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brand);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String brand = "BMW";

    public Car build() {
      Car result = new Car();
      result.brand = this.brand;
      return result;
    }

    public Builder brand(String brand) {
      this.brand = brand;
      return this;
    }
  }
}
