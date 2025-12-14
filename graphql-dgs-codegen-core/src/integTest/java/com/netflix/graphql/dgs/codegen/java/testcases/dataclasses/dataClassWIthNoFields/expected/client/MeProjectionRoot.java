package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWIthNoFields.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class MeProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public MeProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public MeProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }
}
