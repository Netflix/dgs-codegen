package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class WorkshopReviewsProjection extends BaseSubProjectionNode<WorkshopProjectionRoot, WorkshopProjectionRoot> {
  public WorkshopReviewsProjection(WorkshopProjectionRoot parent, WorkshopProjectionRoot root) {
    super(parent, root);
  }

  public WorkshopReviewsEdgesProjection edges() {
    WorkshopReviewsEdgesProjection projection = new WorkshopReviewsEdgesProjection(this, getRoot());    
    getFields().put("edges", projection);
    return projection;
  }
}
