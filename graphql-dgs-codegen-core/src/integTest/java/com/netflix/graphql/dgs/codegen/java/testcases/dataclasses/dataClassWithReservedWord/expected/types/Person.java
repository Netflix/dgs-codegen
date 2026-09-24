package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithReservedWord.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Person {
  private String info;

  private String _interface;

  public Person() {
  }

  public Person(String info, String _interface) {
    this.info = info;
    this._interface = _interface;
  }

  public String getInfo() {
    return info;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getInterface() {
    return _interface;
  }

  public void setInterface(String _interface) {
    this._interface = _interface;
  }

  @Override
  public String toString() {
    return "Person{info='" + info + "', interface='" + _interface + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Person that = (Person) o;
    return Objects.equals(info, that.info) &&
        Objects.equals(_interface, that._interface);
  }

  @Override
  public int hashCode() {
    return Objects.hash(info, _interface);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String info;

    private String _interface;

    public Person build() {
      Person result = new Person();
      result.info = this.info;
      result._interface = this._interface;
      return result;
    }

    public Builder info(String info) {
      this.info = info;
      return this;
    }

    public Builder _interface(String _interface) {
      this._interface = _interface;
      return this;
    }
  }
}
