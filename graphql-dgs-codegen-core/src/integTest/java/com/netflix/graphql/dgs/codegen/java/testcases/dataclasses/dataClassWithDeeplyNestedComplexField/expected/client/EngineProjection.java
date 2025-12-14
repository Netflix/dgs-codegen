package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EngineProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EngineProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Engine"));
  }

  public EngineProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PerformanceProjection<EngineProjection<PARENT, ROOT>, ROOT> performance() {
     PerformanceProjection<EngineProjection<PARENT, ROOT>, ROOT> projection = new PerformanceProjection<>(this, getRoot());
     getFields().put("performance", projection);
     return projection;
  }

  public EngineProjection<PARENT, ROOT> type() {
    getFields().put("type", null);
    return this;
  }

  public EngineProjection<PARENT, ROOT> bhp() {
    getFields().put("bhp", null);
    return this;
  }

  public EngineProjection<PARENT, ROOT> size() {
    getFields().put("size", null);
    return this;
  }
}
