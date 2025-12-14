package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class IsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public IsProjectionRoot() {
    super(null, null, java.util.Optional.of("I"));
  }

  public IsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public IsProjectionRoot<PARENT, ROOT> _id() {
    getFields().put("_id", null);
    return this;
  }

  public TFragmentProjection<IsProjectionRoot<PARENT, ROOT>, IsProjectionRoot<PARENT, ROOT>> onT() {
    TFragmentProjection<IsProjectionRoot<PARENT, ROOT>, IsProjectionRoot<PARENT, ROOT>> fragment = new TFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
