package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultEnumValueForArray.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SomeType {
  private List<Color> colors = Arrays.asList(Color.red);

  public SomeType() {
  }

  public SomeType(List<Color> colors) {
    this.colors = colors;
  }

  public List<Color> getColors() {
    return colors;
  }

  public void setColors(List<Color> colors) {
    this.colors = colors;
  }

  @Override
  public String toString() {
    return "SomeType{colors='" + colors + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SomeType that = (SomeType) o;
    return Objects.equals(colors, that.colors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(colors);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private List<Color> colors = Arrays.asList(Color.red);

    public SomeType build() {
      SomeType result = new SomeType();
      result.colors = this.colors;
      return result;
    }

    public Builder colors(List<Color> colors) {
      this.colors = colors;
      return this;
    }
  }
}
