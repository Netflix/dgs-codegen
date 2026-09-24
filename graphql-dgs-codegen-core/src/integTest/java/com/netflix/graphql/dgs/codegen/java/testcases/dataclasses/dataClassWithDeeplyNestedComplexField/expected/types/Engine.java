package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.types;

import java.lang.Double;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Engine {
  private String type;

  private Integer bhp;

  private Double size;

  private Performance performance;

  public Engine() {
  }

  public Engine(String type, Integer bhp, Double size, Performance performance) {
    this.type = type;
    this.bhp = bhp;
    this.size = size;
    this.performance = performance;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Integer getBhp() {
    return bhp;
  }

  public void setBhp(Integer bhp) {
    this.bhp = bhp;
  }

  public Double getSize() {
    return size;
  }

  public void setSize(Double size) {
    this.size = size;
  }

  public Performance getPerformance() {
    return performance;
  }

  public void setPerformance(Performance performance) {
    this.performance = performance;
  }

  @Override
  public String toString() {
    return "Engine{type='" + type + "', bhp='" + bhp + "', size='" + size + "', performance='" + performance + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Engine that = (Engine) o;
    return Objects.equals(type, that.type) &&
        Objects.equals(bhp, that.bhp) &&
        Objects.equals(size, that.size) &&
        Objects.equals(performance, that.performance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, bhp, size, performance);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String type;

    private Integer bhp;

    private Double size;

    private Performance performance;

    public Engine build() {
      Engine result = new Engine();
      result.type = this.type;
      result.bhp = this.bhp;
      result.size = this.size;
      result.performance = this.performance;
      return result;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    public Builder bhp(Integer bhp) {
      this.bhp = bhp;
      return this;
    }

    public Builder size(Double size) {
      this.size = size;
      return this;
    }

    public Builder performance(Performance performance) {
      this.performance = performance;
      return this;
    }
  }
}
