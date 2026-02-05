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
public class Bird implements com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFields.expected.types.Pet {
  private String id;

  private String name;

  private List<String> address;

  private Bird mother;

  private Bird father;

  private List<Bird> parents;

  public Bird() {
  }

  public Bird(String id, String name, List<String> address, Bird mother, Bird father,
      List<Bird> parents) {
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

  public Bird getMother() {
    return mother;
  }

  public void setMother(Bird mother) {
    this.mother = mother;
  }

  public Bird getFather() {
    return father;
  }

  public void setFather(Bird father) {
    this.father = father;
  }

  public List<Bird> getParents() {
    return parents;
  }

  public void setParents(List<Bird> parents) {
    this.parents = parents;
  }

  @Override
  public String toString() {
    return "Bird{id='" + id + "', name='" + name + "', address='" + address + "', mother='" + mother + "', father='" + father + "', parents='" + parents + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Bird that = (Bird) o;
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

    private Bird mother;

    private Bird father;

    private List<Bird> parents;

    public Bird build() {
      Bird result = new Bird();
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

    public Builder mother(Bird mother) {
      this.mother = mother;
      return this;
    }

    public Builder father(Bird father) {
      this.father = father;
      return this;
    }

    public Builder parents(List<Bird> parents) {
      this.parents = parents;
      return this;
    }
  }
}
