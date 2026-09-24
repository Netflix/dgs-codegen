package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsWithExtendedQuery.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FriendsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FriendsProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public FriendsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public FriendsProjectionRoot<PARENT, ROOT> firstname() {
    getFields().put("firstname", null);
    return this;
  }

  public FriendsProjectionRoot<PARENT, ROOT> lastname() {
    getFields().put("lastname", null);
    return this;
  }
}
