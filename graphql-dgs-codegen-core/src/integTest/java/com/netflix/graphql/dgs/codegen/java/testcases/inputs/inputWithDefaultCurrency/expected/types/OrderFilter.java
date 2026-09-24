package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultCurrency.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Currency;
import java.util.Objects;

public class OrderFilter {
  private Currency value = Currency.getInstance("USD");

  public OrderFilter() {
  }

  public OrderFilter(Currency value) {
    this.value = value;
  }

  public Currency getValue() {
    return value;
  }

  public void setValue(Currency value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "OrderFilter{value='" + value + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderFilter that = (OrderFilter) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Currency value = Currency.getInstance("USD");

    public OrderFilter build() {
      OrderFilter result = new OrderFilter();
      result.value = this.value;
      return result;
    }

    public Builder value(Currency value) {
      this.value = value;
      return this;
    }
  }
}
