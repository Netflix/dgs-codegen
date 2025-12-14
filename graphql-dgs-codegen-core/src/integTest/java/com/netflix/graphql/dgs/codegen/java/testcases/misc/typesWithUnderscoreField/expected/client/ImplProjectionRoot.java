package com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class ImplProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public ImplProjectionRoot() {
    super(null, null, java.util.Optional.of("MyInterfaceImpl"));
  }

  public ImplProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public ImplProjectionRoot<PARENT, ROOT> __() {
    getFields().put("_", null);
    return this;
  }
}
