package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class T implements com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.types.I {
  private String _id;

  private String id;

  public T() {
  }

  public T(String _id, String id) {
    this._id = _id;
    this.id = id;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "T{_id='" + _id + "', id='" + id + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    T that = (T) o;
    return Objects.equals(_id, that._id) &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_id, id);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String _id;

    private String id;

    public T build() {
      T result = new T();
      result._id = this._id;
      result.id = this.id;
      return result;
    }

    public Builder _id(String _id) {
      this._id = _id;
      return this;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }
  }
}
