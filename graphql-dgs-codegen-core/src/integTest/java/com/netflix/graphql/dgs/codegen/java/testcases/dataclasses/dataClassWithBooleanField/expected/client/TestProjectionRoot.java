package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithBooleanField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class TestProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public TestProjectionRoot() {
    super(null, null, java.util.Optional.of("RequiredTestType"));
  }

  public TestProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public TestProjectionRoot<PARENT, ROOT> isRequired() {
    getFields().put("isRequired", null);
    return this;
  }
}
