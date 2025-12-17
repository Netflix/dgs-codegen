package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultBigDecimal.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.math.BigDecimal;
import java.util.Objects;

public class OrderFilter {
  private BigDecimal min = new BigDecimal("1.1");

  private BigDecimal avg = new BigDecimal(1.12);

  private BigDecimal max = new BigDecimal(3.14E+19);

  public OrderFilter() {
  }

  public OrderFilter(BigDecimal min, BigDecimal avg, BigDecimal max) {
    this.min = min;
    this.avg = avg;
    this.max = max;
  }

  public BigDecimal getMin() {
    return min;
  }

  public void setMin(BigDecimal min) {
    this.min = min;
  }

  public BigDecimal getAvg() {
    return avg;
  }

  public void setAvg(BigDecimal avg) {
    this.avg = avg;
  }

  public BigDecimal getMax() {
    return max;
  }

  public void setMax(BigDecimal max) {
    this.max = max;
  }

  @Override
  public String toString() {
    return "OrderFilter{min='" + min + "', avg='" + avg + "', max='" + max + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderFilter that = (OrderFilter) o;
    return Objects.equals(min, that.min) &&
        Objects.equals(avg, that.avg) &&
        Objects.equals(max, that.max);
  }

  @Override
  public int hashCode() {
    return Objects.hash(min, avg, max);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private BigDecimal min = new BigDecimal("1.1");

    private BigDecimal avg = new BigDecimal(1.12);

    private BigDecimal max = new BigDecimal(3.14E+19);

    public OrderFilter build() {
      OrderFilter result = new OrderFilter();
      result.min = this.min;
      result.avg = this.avg;
      result.max = this.max;
      return result;
    }

    public Builder min(BigDecimal min) {
      this.min = min;
      return this;
    }

    public Builder avg(BigDecimal avg) {
      this.avg = avg;
      return this;
    }

    public Builder max(BigDecimal max) {
      this.max = max;
      return this;
    }
  }
}
