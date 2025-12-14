package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForNonNullableFields.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Person {
  private String name = "Damian";

  private int age = 18;

  private Car car = new Car(){{setBrand("Ford");}};

  private List<Hobby> hobbies = Arrays.asList(Hobby.Hokey);

  private boolean isHappy = true;

  public Person() {
  }

  public Person(String name, int age, Car car, List<Hobby> hobbies, boolean isHappy) {
    this.name = name;
    this.age = age;
    this.car = car;
    this.hobbies = hobbies;
    this.isHappy = isHappy;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public Car getCar() {
    return car;
  }

  public void setCar(Car car) {
    this.car = car;
  }

  public List<Hobby> getHobbies() {
    return hobbies;
  }

  public void setHobbies(List<Hobby> hobbies) {
    this.hobbies = hobbies;
  }

  public boolean getIsHappy() {
    return isHappy;
  }

  public void setIsHappy(boolean isHappy) {
    this.isHappy = isHappy;
  }

  @Override
  public String toString() {
    return "Person{name='" + name + "', age='" + age + "', car='" + car + "', hobbies='" + hobbies + "', isHappy='" + isHappy + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(name, that.name) &&
        age == that.age &&
        Objects.equals(car, that.car) &&
        Objects.equals(hobbies, that.hobbies) &&
        isHappy == that.isHappy;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age, car, hobbies, isHappy);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String name = "Damian";

    private int age = 18;

    private Car car = new Car(){{setBrand("Ford");}};

    private List<Hobby> hobbies = Arrays.asList(Hobby.Hokey);

    private boolean isHappy = true;

    public Person build() {
      Person result = new Person();
      result.name = this.name;
      result.age = this.age;
      result.car = this.car;
      result.hobbies = this.hobbies;
      result.isHappy = this.isHappy;
      return result;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder age(int age) {
      this.age = age;
      return this;
    }

    public Builder car(Car car) {
      this.car = car;
      return this;
    }

    public Builder hobbies(List<Hobby> hobbies) {
      this.hobbies = hobbies;
      return this;
    }

    public Builder isHappy(boolean isHappy) {
      this.isHappy = isHappy;
      return this;
    }
  }
}
