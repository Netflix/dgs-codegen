package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PersonProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PersonProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public PersonProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PersonProjectionRoot<PARENT, ROOT> firstname() {
    getFields().put("firstname", null);
    return this;
  }

  public EmployeeFragmentProjection<PersonProjectionRoot<PARENT, ROOT>, PersonProjectionRoot<PARENT, ROOT>> onEmployee(
      ) {
    EmployeeFragmentProjection<PersonProjectionRoot<PARENT, ROOT>, PersonProjectionRoot<PARENT, ROOT>> fragment = new EmployeeFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
