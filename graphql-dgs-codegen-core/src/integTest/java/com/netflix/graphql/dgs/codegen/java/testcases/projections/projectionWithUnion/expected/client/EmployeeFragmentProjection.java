package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;
import java.lang.Override;
import java.lang.String;

public class EmployeeFragmentProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  {
    getFields().put("__typename", null);
  }

  public EmployeeFragmentProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Employee"));
  }

  public EmployeeFragmentProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public EmployeeFragmentProjection<PARENT, ROOT> firstname() {
    getFields().put("firstname", null);
    return this;
  }

  public EmployeeFragmentProjection<PARENT, ROOT> company() {
    getFields().put("company", null);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("... on Employee {");
    getFields().forEach((k, v) -> {
        builder.append(" ").append(k);
        if(v != null) {
            builder.append(" ").append(v.toString());
        }
    });
    builder.append("}");
     
    return builder.toString();
  }
}
