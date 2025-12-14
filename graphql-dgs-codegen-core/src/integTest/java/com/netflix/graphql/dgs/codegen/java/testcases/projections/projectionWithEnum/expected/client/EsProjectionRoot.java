package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithEnum.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EsProjectionRoot() {
    super(null, null, java.util.Optional.of("E"));
  }

  public EsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }
}
