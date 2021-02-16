package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class WorkshopAssetsReviewsProjection extends BaseSubProjectionNode<WorkshopAssetsProjection, WorkshopProjectionRoot> {
  public WorkshopAssetsReviewsProjection(WorkshopAssetsProjection parent,
      WorkshopProjectionRoot root) {
    super(parent, root);
  }

  public WorkshopAssetsReviewsEdgesProjection edges() {
    WorkshopAssetsReviewsEdgesProjection projection = new WorkshopAssetsReviewsEdgesProjection(this, getRoot());    
    getFields().put("edges", projection);
    return projection;
  }
}
