package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedInterfaces.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class ProductsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public ProductsProjectionRoot() {
    super(null, null, java.util.Optional.of("Product"));
  }

  public ProductsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public ProductsProjectionRoot<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }
}
