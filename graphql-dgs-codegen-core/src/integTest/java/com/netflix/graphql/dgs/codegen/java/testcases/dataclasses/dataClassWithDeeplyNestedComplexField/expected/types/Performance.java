package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.types;

import java.lang.Double;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Performance {
  private Double zeroToSixty;

  private Double quarterMile;

  public Performance() {
  }

  public Performance(Double zeroToSixty, Double quarterMile) {
    this.zeroToSixty = zeroToSixty;
    this.quarterMile = quarterMile;
  }

  public Double getZeroToSixty() {
    return zeroToSixty;
  }

  public void setZeroToSixty(Double zeroToSixty) {
    this.zeroToSixty = zeroToSixty;
  }

  public Double getQuarterMile() {
    return quarterMile;
  }

  public void setQuarterMile(Double quarterMile) {
    this.quarterMile = quarterMile;
  }

  @Override
  public String toString() {
    return "Performance{zeroToSixty='" + zeroToSixty + "', quarterMile='" + quarterMile + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Performance that = (Performance) o;
    return Objects.equals(zeroToSixty, that.zeroToSixty) &&
        Objects.equals(quarterMile, that.quarterMile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(zeroToSixty, quarterMile);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Double zeroToSixty;

    private Double quarterMile;

    public Performance build() {
      Performance result = new Performance();
      result.zeroToSixty = this.zeroToSixty;
      result.quarterMile = this.quarterMile;
      return result;
    }

    public Builder zeroToSixty(Double zeroToSixty) {
      this.zeroToSixty = zeroToSixty;
      return this;
    }

    public Builder quarterMile(Double quarterMile) {
      this.quarterMile = quarterMile;
      return this;
    }
  }
}
