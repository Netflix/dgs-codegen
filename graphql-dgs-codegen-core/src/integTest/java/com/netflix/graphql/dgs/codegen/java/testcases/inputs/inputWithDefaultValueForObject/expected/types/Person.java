package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForObject.expected.types;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Person {
  private String name = "John";

  private Integer age = 23;

  private Car car = new Car(){{setBrand("Ford");}};

  public Person() {
  }

  public Person(String name, Integer age, Car car) {
    this.name = name;
    this.age = age;
    this.car = car;
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

  public Car getCar() {
    return car;
  }

  public void setCar(Car car) {
    this.car = car;
  }

  @Override
  public String toString() {
    return "Person{name='" + name + "', age='" + age + "', car='" + car + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(age, that.age) &&
        Objects.equals(car, that.car);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age, car);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name = "John";

    private Integer age = 23;

    private Car car = new Car(){{setBrand("Ford");}};

    public Person build() {
      Person result = new Person();
      result.name = this.name;
      result.age = this.age;
      result.car = this.car;
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

    public Builder car(Car car) {
      this.car = car;
      return this;
    }
  }
}
