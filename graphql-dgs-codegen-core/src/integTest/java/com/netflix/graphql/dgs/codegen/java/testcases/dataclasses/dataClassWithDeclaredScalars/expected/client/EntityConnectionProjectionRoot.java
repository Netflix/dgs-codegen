package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntityConnectionProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntityConnectionProjectionRoot() {
    super(null, null, java.util.Optional.of("EntityConnection"));
  }

  public EntityConnectionProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PageInfoProjection<EntityConnectionProjectionRoot<PARENT, ROOT>, EntityConnectionProjectionRoot<PARENT, ROOT>> pageInfo(
      ) {
    PageInfoProjection<EntityConnectionProjectionRoot<PARENT, ROOT>, EntityConnectionProjectionRoot<PARENT, ROOT>> projection = new PageInfoProjection<>(this, this);    
    getFields().put("pageInfo", projection);
    return projection;
  }

  public EntityEdgeProjection<EntityConnectionProjectionRoot<PARENT, ROOT>, EntityConnectionProjectionRoot<PARENT, ROOT>> edges(
      ) {
    EntityEdgeProjection<EntityConnectionProjectionRoot<PARENT, ROOT>, EntityConnectionProjectionRoot<PARENT, ROOT>> projection = new EntityEdgeProjection<>(this, this);    
    getFields().put("edges", projection);
    return projection;
  }
}
