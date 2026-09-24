package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Car {
  private String make;

  private String model;

  private Engine engine;

  public Car() {
  }

  public Car(String make, String model, Engine engine) {
    this.make = make;
    this.model = model;
    this.engine = engine;
  }

  public String getMake() {
    return make;
  }

  public void setMake(String make) {
    this.make = make;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Engine getEngine() {
    return engine;
  }

  public void setEngine(Engine engine) {
    this.engine = engine;
  }

  @Override
  public String toString() {
    return "Car{make='" + make + "', model='" + model + "', engine='" + engine + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Car that = (Car) o;
    return Objects.equals(make, that.make) &&
        Objects.equals(model, that.model) &&
        Objects.equals(engine, that.engine);
  }

  @Override
  public int hashCode() {
    return Objects.hash(make, model, engine);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String make;

    private String model;

    private Engine engine;

    public Car build() {
      Car result = new Car();
      result.make = this.make;
      result.model = this.model;
      result.engine = this.engine;
      return result;
    }

    public Builder make(String make) {
      this.make = make;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder engine(Engine engine) {
      this.engine = engine;
      return this;
    }
  }
}
