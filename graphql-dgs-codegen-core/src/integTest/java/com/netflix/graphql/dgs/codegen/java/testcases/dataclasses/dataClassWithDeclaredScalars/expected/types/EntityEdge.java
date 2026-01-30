package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class EntityEdge {
  private String cursor;

  private Entity node;

  public EntityEdge() {
  }

  public EntityEdge(String cursor, Entity node) {
    this.cursor = cursor;
    this.node = node;
  }

  public String getCursor() {
    return cursor;
  }

  public void setCursor(String cursor) {
    this.cursor = cursor;
  }

  public Entity getNode() {
    return node;
  }

  public void setNode(Entity node) {
    this.node = node;
  }

  @Override
  public String toString() {
    return "EntityEdge{cursor='" + cursor + "', node='" + node + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityEdge that = (EntityEdge) o;
    return Objects.equals(cursor, that.cursor) &&
        Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cursor, node);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String cursor;

    private Entity node;

    public EntityEdge build() {
      EntityEdge result = new EntityEdge();
      result.cursor = this.cursor;
      result.node = this.node;
      return result;
    }

    public Builder cursor(String cursor) {
      this.cursor = cursor;
      return this;
    }

    public Builder node(Entity node) {
      this.node = node;
      return this;
    }
  }
}
