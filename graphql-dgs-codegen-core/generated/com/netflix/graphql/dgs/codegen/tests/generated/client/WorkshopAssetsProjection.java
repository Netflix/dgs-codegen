package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class WorkshopAssetsProjection extends BaseSubProjectionNode<WorkshopProjectionRoot, WorkshopProjectionRoot> {
  public WorkshopAssetsProjection(WorkshopProjectionRoot parent, WorkshopProjectionRoot root) {
    super(parent, root);
  }

  public WorkshopAssetsReviewsProjection reviews() {
    WorkshopAssetsReviewsProjection projection = new WorkshopAssetsReviewsProjection(this, getRoot());    
    getFields().put("reviews", projection);
    return projection;
  }
}
