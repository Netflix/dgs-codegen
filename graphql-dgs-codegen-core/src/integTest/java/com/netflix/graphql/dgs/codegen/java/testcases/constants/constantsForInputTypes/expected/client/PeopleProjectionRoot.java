package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsForInputTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PeopleProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PeopleProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public PeopleProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PeopleProjectionRoot<PARENT, ROOT> firstname() {
    getFields().put("firstname", null);
    return this;
  }

  public PeopleProjectionRoot<PARENT, ROOT> lastname() {
    getFields().put("lastname", null);
    return this;
  }
}
