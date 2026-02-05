package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFields.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Dog implements com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFields.expected.types.Pet {
  private String id;

  private String name;

  private List<String> address;

  private Dog mother;

  private Dog father;

  private List<Dog> parents;

  public Dog() {
  }

  public Dog(String id, String name, List<String> address, Dog mother, Dog father,
      List<Dog> parents) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.mother = mother;
    this.father = father;
    this.parents = parents;
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

  public List<String> getAddress() {
    return address;
  }

  public void setAddress(List<String> address) {
    this.address = address;
  }

  public Dog getMother() {
    return mother;
  }

  public void setMother(Dog mother) {
    this.mother = mother;
  }

  public Dog getFather() {
    return father;
  }

  public void setFather(Dog father) {
    this.father = father;
  }

  public List<Dog> getParents() {
    return parents;
  }

  public void setParents(List<Dog> parents) {
    this.parents = parents;
  }

  @Override
  public String toString() {
    return "Dog{id='" + id + "', name='" + name + "', address='" + address + "', mother='" + mother + "', father='" + father + "', parents='" + parents + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dog that = (Dog) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(name, that.name) &&
        Objects.equals(address, that.address) &&
        Objects.equals(mother, that.mother) &&
        Objects.equals(father, that.father) &&
        Objects.equals(parents, that.parents);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, address, mother, father, parents);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String name;

    private List<String> address;

    private Dog mother;

    private Dog father;

    private List<Dog> parents;

    public Dog build() {
      Dog result = new Dog();
      result.id = this.id;
      result.name = this.name;
      result.address = this.address;
      result.mother = this.mother;
      result.father = this.father;
      result.parents = this.parents;
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

    public Builder address(List<String> address) {
      this.address = address;
      return this;
    }

    public Builder mother(Dog mother) {
      this.mother = mother;
      return this;
    }

    public Builder father(Dog father) {
      this.father = father;
      return this;
    }

    public Builder parents(List<Dog> parents) {
      this.parents = parents;
      return this;
    }
  }
}
