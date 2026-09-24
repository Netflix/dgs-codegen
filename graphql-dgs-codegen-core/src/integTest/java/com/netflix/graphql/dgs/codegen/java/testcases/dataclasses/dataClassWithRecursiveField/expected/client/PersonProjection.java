package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithRecursiveField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PersonProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PersonProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Person"));
  }

  public PersonProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PersonProjection<PersonProjection<PARENT, ROOT>, ROOT> friends() {
     PersonProjection<PersonProjection<PARENT, ROOT>, ROOT> projection = new PersonProjection<>(this, getRoot());
     getFields().put("friends", projection);
     return projection;
  }

  public PersonProjection<PARENT, ROOT> firstname() {
    getFields().put("firstname", null);
    return this;
  }

  public PersonProjection<PARENT, ROOT> lastname() {
    getFields().put("lastname", null);
    return this;
  }
}
