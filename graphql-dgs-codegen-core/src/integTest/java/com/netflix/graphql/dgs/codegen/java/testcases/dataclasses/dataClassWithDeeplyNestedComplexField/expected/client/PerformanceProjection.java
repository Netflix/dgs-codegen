package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PerformanceProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PerformanceProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Performance"));
  }

  public PerformanceProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PerformanceProjection<PARENT, ROOT> zeroToSixty() {
    getFields().put("zeroToSixty", null);
    return this;
  }

  public PerformanceProjection<PARENT, ROOT> quarterMile() {
    getFields().put("quarterMile", null);
    return this;
  }
}
