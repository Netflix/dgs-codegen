package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForEnum.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class ColorFilter {
  private Color color = Color.red;

  public ColorFilter() {
  }

  public ColorFilter(Color color) {
    this.color = color;
  }

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public String toString() {
    return "ColorFilter{color='" + color + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ColorFilter that = (ColorFilter) o;
    return Objects.equals(color, that.color);
  }

  @Override
  public int hashCode() {
    return Objects.hash(color);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Color color = Color.red;

    public ColorFilter build() {
      ColorFilter result = new ColorFilter();
      result.color = this.color;
      return result;
    }

    public Builder color(Color color) {
      this.color = color;
      return this;
    }
  }
}
