package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class WorkshopAssetsReviewsEdgesProjection extends BaseSubProjectionNode<WorkshopAssetsReviewsProjection, WorkshopProjectionRoot> {
  public WorkshopAssetsReviewsEdgesProjection(WorkshopAssetsReviewsProjection parent,
      WorkshopProjectionRoot root) {
    super(parent, root);
  }

  public WorkshopAssetsReviewsEdgesProjection node() {
    getFields().put("node", null);
    return this;
  }
}
