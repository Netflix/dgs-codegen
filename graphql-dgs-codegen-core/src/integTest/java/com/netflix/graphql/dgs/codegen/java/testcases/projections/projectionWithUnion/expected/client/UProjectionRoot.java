package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class UProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public UProjectionRoot() {
    super(null, null, java.util.Optional.of("U"));
  }

  public UProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EmployeeFragmentProjection<UProjectionRoot<PARENT, ROOT>, UProjectionRoot<PARENT, ROOT>> onEmployee(
      ) {
    EmployeeFragmentProjection<UProjectionRoot<PARENT, ROOT>, UProjectionRoot<PARENT, ROOT>> fragment = new EmployeeFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
