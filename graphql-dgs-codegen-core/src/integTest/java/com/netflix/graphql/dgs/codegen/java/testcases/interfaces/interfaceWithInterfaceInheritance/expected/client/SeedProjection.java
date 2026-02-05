package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class SeedProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public SeedProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Seed"));
  }

  public SeedProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public SeedProjection<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }
}
