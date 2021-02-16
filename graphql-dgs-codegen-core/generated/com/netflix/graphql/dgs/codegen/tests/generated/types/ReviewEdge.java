package com.netflix.graphql.dgs.codegen.tests.generated.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;

public class ReviewEdge {
  private String node;

  public ReviewEdge() {
  }

  public ReviewEdge(String node) {
    this.node = node;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  @Override
  public String toString() {
    return "ReviewEdge{" + "node='" + node + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewEdge that = (ReviewEdge) o;
        return java.util.Objects.equals(node, that.node);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(node);
  }

  public static com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewEdge.Builder newBuilder(
      ) {
    return new Builder();
  }

  public static class Builder {
    private String node;

    public ReviewEdge build() {
      com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewEdge result = new com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewEdge();
          result.node = this.node;
          return result;
    }

    public com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewEdge.Builder node(
        String node) {
      this.node = node;
      return this;
    }
  }
}
