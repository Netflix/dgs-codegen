package com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class MyInterfaceImpl implements com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.types.MyInterface {
  private String __;

  public MyInterfaceImpl() {
  }

  public MyInterfaceImpl(String __) {
    this.__ = __;
  }

  public String get_() {
    return __;
  }

  public void set_(String __) {
    this.__ = __;
  }

  @Override
  public String toString() {
    return "MyInterfaceImpl{_='" + __ + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MyInterfaceImpl that = (MyInterfaceImpl) o;
    return Objects.equals(__, that.__);
  }

  @Override
  public int hashCode() {
    return Objects.hash(__);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String __;

    public MyInterfaceImpl build() {
      MyInterfaceImpl result = new MyInterfaceImpl();
      result.__ = this.__;
      return result;
    }

    public Builder __(String __) {
      this.__ = __;
      return this;
    }
  }
}
