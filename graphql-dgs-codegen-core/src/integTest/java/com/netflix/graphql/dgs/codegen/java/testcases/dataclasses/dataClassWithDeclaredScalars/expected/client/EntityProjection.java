package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntityProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntityProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Entity"));
  }

  public EntityProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EntityProjection<PARENT, ROOT> _long() {
    getFields().put("long", null);
    return this;
  }

  public EntityProjection<PARENT, ROOT> dateTime() {
    getFields().put("dateTime", null);
    return this;
  }
}
