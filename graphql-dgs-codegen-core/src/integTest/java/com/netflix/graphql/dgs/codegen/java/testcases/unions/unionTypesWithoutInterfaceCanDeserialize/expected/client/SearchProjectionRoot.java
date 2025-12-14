package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class SearchProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public SearchProjectionRoot() {
    super(null, null, java.util.Optional.of("SearchResultPage"));
  }

  public SearchProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public SearchResultProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> items(
      ) {
    SearchResultProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> projection = new SearchResultProjection<>(this, this);    
    getFields().put("items", projection);
    return projection;
  }
}
