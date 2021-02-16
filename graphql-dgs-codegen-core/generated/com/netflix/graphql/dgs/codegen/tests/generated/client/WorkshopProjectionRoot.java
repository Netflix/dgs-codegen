package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseProjectionNode;

public class WorkshopProjectionRoot extends BaseProjectionNode {
  public WorkshopReviewsProjection reviews() {
    WorkshopReviewsProjection projection = new WorkshopReviewsProjection(this, this);    
    getFields().put("reviews", projection);
    return projection;
  }

  public WorkshopAssetsProjection assets() {
    WorkshopAssetsProjection projection = new WorkshopAssetsProjection(this, this);    
    getFields().put("assets", projection);
    return projection;
  }
}
