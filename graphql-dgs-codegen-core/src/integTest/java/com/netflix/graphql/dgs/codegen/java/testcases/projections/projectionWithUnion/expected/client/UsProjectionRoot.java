package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class UsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public UsProjectionRoot() {
    super(null, null, java.util.Optional.of("U"));
  }

  public UsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EmployeeFragmentProjection<UsProjectionRoot<PARENT, ROOT>, UsProjectionRoot<PARENT, ROOT>> onEmployee(
      ) {
    EmployeeFragmentProjection<UsProjectionRoot<PARENT, ROOT>, UsProjectionRoot<PARENT, ROOT>> fragment = new EmployeeFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
