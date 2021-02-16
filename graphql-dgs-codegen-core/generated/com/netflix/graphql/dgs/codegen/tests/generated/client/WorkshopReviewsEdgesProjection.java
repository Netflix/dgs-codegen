package com.netflix.graphql.dgs.codegen.tests.generated.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class WorkshopReviewsEdgesProjection extends BaseSubProjectionNode<WorkshopReviewsProjection, WorkshopProjectionRoot> {
  public WorkshopReviewsEdgesProjection(WorkshopReviewsProjection parent,
      WorkshopProjectionRoot root) {
    super(parent, root);
  }

  public WorkshopReviewsEdgesProjection node() {
    getFields().put("node", null);
    return this;
  }
}
