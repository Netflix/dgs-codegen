package com.netflix.graphql.dgs.codegen.tests.generated.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;

public class ReviewConnection {
  private List<ReviewEdge> edges;

  public ReviewConnection() {
  }

  public ReviewConnection(List<ReviewEdge> edges) {
    this.edges = edges;
  }

  public List<ReviewEdge> getEdges() {
    return edges;
  }

  public void setEdges(List<ReviewEdge> edges) {
    this.edges = edges;
  }

  @Override
  public String toString() {
    return "ReviewConnection{" + "edges='" + edges + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewConnection that = (ReviewConnection) o;
        return java.util.Objects.equals(edges, that.edges);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(edges);
  }

  public static com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewConnection.Builder newBuilder(
      ) {
    return new Builder();
  }

  public static class Builder {
    private List<ReviewEdge> edges;

    public ReviewConnection build() {
      com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewConnection result = new com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewConnection();
          result.edges = this.edges;
          return result;
    }

    public com.netflix.graphql.dgs.codegen.tests.generated.types.ReviewConnection.Builder edges(
        List<ReviewEdge> edges) {
      this.edges = edges;
      return this;
    }
  }
}
