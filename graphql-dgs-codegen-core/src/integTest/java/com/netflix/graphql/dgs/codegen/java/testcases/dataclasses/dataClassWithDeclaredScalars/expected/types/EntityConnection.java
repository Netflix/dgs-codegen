package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

public class EntityConnection {
  private PageInfo pageInfo;

  private List<EntityEdge> edges;

  public EntityConnection() {
  }

  public EntityConnection(PageInfo pageInfo, List<EntityEdge> edges) {
    this.pageInfo = pageInfo;
    this.edges = edges;
  }

  public PageInfo getPageInfo() {
    return pageInfo;
  }

  public void setPageInfo(PageInfo pageInfo) {
    this.pageInfo = pageInfo;
  }

  public List<EntityEdge> getEdges() {
    return edges;
  }

  public void setEdges(List<EntityEdge> edges) {
    this.edges = edges;
  }

  @Override
  public String toString() {
    return "EntityConnection{pageInfo='" + pageInfo + "', edges='" + edges + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EntityConnection that = (EntityConnection) o;
    return Objects.equals(pageInfo, that.pageInfo) &&
        Objects.equals(edges, that.edges);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pageInfo, edges);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private PageInfo pageInfo;

    private List<EntityEdge> edges;

    public EntityConnection build() {
      EntityConnection result = new EntityConnection();
      result.pageInfo = this.pageInfo;
      result.edges = this.edges;
      return result;
    }

    public Builder pageInfo(PageInfo pageInfo) {
      this.pageInfo = pageInfo;
      return this;
    }

    public Builder edges(List<EntityEdge> edges) {
      this.edges = edges;
      return this;
    }
  }
}
