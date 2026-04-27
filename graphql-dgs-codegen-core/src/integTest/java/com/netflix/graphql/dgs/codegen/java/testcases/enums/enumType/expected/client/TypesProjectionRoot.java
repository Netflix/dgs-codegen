package com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;
import com.netflix.graphql.dgs.codegen.java.testcases.enums.enumType.expected.Generated;

@Generated
public class TypesProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public TypesProjectionRoot() {
    super(null, null, java.util.Optional.of("EmployeeTypes"));
  }

  public TypesProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }
}
