package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionWithExtendedType.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;
import com.netflix.graphql.dgs.codegen.java.testcases.unions.unionWithExtendedType.expected.Generated;

@Generated
public class SearchProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public SearchProjectionRoot() {
    super(null, null, java.util.Optional.of("SearchResult"));
  }

  public SearchProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public MovieFragmentProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> onMovie(
      ) {
    MovieFragmentProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> fragment = new MovieFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }

  public ActorFragmentProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> onActor(
      ) {
    ActorFragmentProjection<SearchProjectionRoot<PARENT, ROOT>, SearchProjectionRoot<PARENT, ROOT>> fragment = new ActorFragmentProjection<>(this, this);
    getFragments().add(fragment);
    return fragment;
  }
}
