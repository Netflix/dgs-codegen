package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EProjectionRoot() {
    super(null, null, java.util.Optional.of("E"));
  }

  public EProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }
}
