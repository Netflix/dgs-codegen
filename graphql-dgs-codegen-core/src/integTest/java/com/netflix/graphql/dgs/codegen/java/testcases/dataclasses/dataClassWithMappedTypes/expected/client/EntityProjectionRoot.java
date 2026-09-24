package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntityProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntityProjectionRoot() {
    super(null, null, java.util.Optional.of("Entity"));
  }

  public EntityProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EntityProjectionRoot<PARENT, ROOT> _long() {
    getFields().put("long", null);
    return this;
  }

  public EntityProjectionRoot<PARENT, ROOT> dateTime() {
    getFields().put("dateTime", null);
    return this;
  }
}
