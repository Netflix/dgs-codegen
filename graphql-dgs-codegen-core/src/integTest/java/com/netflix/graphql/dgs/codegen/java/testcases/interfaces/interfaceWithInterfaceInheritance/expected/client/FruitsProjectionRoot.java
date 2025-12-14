package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FruitsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FruitsProjectionRoot() {
    super(null, null, java.util.Optional.of("Fruit"));
  }

  public FruitsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public SeedProjection<FruitsProjectionRoot<PARENT, ROOT>, FruitsProjectionRoot<PARENT, ROOT>> seeds(
      ) {
    SeedProjection<FruitsProjectionRoot<PARENT, ROOT>, FruitsProjectionRoot<PARENT, ROOT>> projection = new SeedProjection<>(this, this);    
    getFields().put("seeds", projection);
    return projection;
  }
}
