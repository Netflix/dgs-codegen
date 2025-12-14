package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class CarsProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public CarsProjectionRoot() {
    super(null, null, java.util.Optional.of("Car"));
  }

  public CarsProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EngineProjection<CarsProjectionRoot<PARENT, ROOT>, CarsProjectionRoot<PARENT, ROOT>> engine(
      ) {
    EngineProjection<CarsProjectionRoot<PARENT, ROOT>, CarsProjectionRoot<PARENT, ROOT>> projection = new EngineProjection<>(this, this);    
    getFields().put("engine", projection);
    return projection;
  }

  public CarsProjectionRoot<PARENT, ROOT> make() {
    getFields().put("make", null);
    return this;
  }

  public CarsProjectionRoot<PARENT, ROOT> model() {
    getFields().put("model", null);
    return this;
  }
}
