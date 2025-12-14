package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types;

import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Entity {
  private Long _long;

  private OffsetDateTime dateTime;

  public Entity() {
  }

  public Entity(Long _long, OffsetDateTime dateTime) {
    this._long = _long;
    this.dateTime = dateTime;
  }

  public Long getLong() {
    return _long;
  }

  public void setLong(Long _long) {
    this._long = _long;
  }

  public OffsetDateTime getDateTime() {
    return dateTime;
  }

  public void setDateTime(OffsetDateTime dateTime) {
    this.dateTime = dateTime;
  }

  @Override
  public String toString() {
    return "Entity{long='" + _long + "', dateTime='" + dateTime + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Entity that = (Entity) o;
    return Objects.equals(_long, that._long) &&
        Objects.equals(dateTime, that.dateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_long, dateTime);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Long _long;

    private OffsetDateTime dateTime;

    public Entity build() {
      Entity result = new Entity();
      result._long = this._long;
      result.dateTime = this.dateTime;
      return result;
    }

    public Builder _long(Long _long) {
      this._long = _long;
      return this;
    }

    public Builder dateTime(OffsetDateTime dateTime) {
      this.dateTime = dateTime;
      return this;
    }
  }
}
