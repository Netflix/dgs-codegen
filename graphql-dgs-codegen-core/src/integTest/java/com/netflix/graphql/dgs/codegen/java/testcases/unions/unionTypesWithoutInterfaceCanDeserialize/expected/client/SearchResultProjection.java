package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class SearchResultProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public SearchResultProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("SearchResult"));
  }

  public SearchResultProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public HumanFragmentProjection<SearchResultProjection<PARENT, ROOT>, ROOT> onHuman() {
    HumanFragmentProjection<SearchResultProjection<PARENT, ROOT>, ROOT> fragment = new HumanFragmentProjection<>(this, getRoot());
    getFragments().add(fragment);
    return fragment;
  }

  public DroidFragmentProjection<SearchResultProjection<PARENT, ROOT>, ROOT> onDroid() {
    DroidFragmentProjection<SearchResultProjection<PARENT, ROOT>, ROOT> fragment = new DroidFragmentProjection<>(this, getRoot());
    getFragments().add(fragment);
    return fragment;
  }
}
