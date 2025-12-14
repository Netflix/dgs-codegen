package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithListProperties.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PeopleProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PeopleProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public PeopleProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PeopleProjectionRoot<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public PeopleProjectionRoot<PARENT, ROOT> email() {
    getFields().put("email", null);
    return this;
  }
}
