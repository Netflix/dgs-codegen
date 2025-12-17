package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithReservedWord.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class StringProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public StringProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("String"));
  }

  public StringProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }
}
