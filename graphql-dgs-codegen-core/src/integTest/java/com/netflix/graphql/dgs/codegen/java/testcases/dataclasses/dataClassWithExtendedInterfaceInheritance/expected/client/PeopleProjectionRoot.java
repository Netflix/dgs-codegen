package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithExtendedInterfaceInheritance.expected.client;

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

  public PeopleProjectionRoot<PARENT, ROOT> age() {
    getFields().put("age", null);
    return this;
  }

  public EmployeeFragmentProjection<PeopleProjectionRoot<PARENT, ROOT>, PeopleProjectionRoot<PARENT, ROOT>> onEmployee(
      ) {
    EmployeeFragmentProjection<PeopleProjectionRoot<PARENT, ROOT>, PeopleProjectionRoot<PARENT, ROOT>> fragment = new EmployeeFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
