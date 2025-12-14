package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntityEdgeProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntityEdgeProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("EntityEdge"));
  }

  public EntityEdgeProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EntityProjection<EntityEdgeProjection<PARENT, ROOT>, ROOT> node() {
     EntityProjection<EntityEdgeProjection<PARENT, ROOT>, ROOT> projection = new EntityProjection<>(this, getRoot());
     getFields().put("node", projection);
     return projection;
  }

  public EntityEdgeProjection<PARENT, ROOT> cursor() {
    getFields().put("cursor", null);
    return this;
  }
}
