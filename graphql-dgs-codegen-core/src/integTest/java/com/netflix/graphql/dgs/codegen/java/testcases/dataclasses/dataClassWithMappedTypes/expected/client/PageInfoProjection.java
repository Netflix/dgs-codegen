package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class PageInfoProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PageInfoProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("PageInfo"));
  }

  public PageInfoProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public PageInfoProjection<PARENT, ROOT> startCursor() {
    getFields().put("startCursor", null);
    return this;
  }

  public PageInfoProjection<PARENT, ROOT> endCursor() {
    getFields().put("endCursor", null);
    return this;
  }

  public PageInfoProjection<PARENT, ROOT> hasNextPage() {
    getFields().put("hasNextPage", null);
    return this;
  }

  public PageInfoProjection<PARENT, ROOT> hasPreviousPage() {
    getFields().put("hasPreviousPage", null);
    return this;
  }
}
