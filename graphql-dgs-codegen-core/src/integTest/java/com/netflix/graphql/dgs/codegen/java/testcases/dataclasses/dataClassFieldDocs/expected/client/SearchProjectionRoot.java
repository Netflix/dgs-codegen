package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class SearchProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public SearchProjectionRoot() {
    super(null, null, java.util.Optional.of("Movie"));
  }

  public SearchProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public SearchProjectionRoot<PARENT, ROOT> title() {
    getFields().put("title", null);
    return this;
  }
}
