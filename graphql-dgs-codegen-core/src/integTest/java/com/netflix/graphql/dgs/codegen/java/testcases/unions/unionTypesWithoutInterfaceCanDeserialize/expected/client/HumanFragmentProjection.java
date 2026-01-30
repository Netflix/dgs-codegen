package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;
import java.lang.Override;
import java.lang.String;

public class HumanFragmentProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  {
    getFields().put("__typename", null);
  }

  public HumanFragmentProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("Human"));
  }

  public HumanFragmentProjection<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public HumanFragmentProjection<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  public HumanFragmentProjection<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public HumanFragmentProjection<PARENT, ROOT> totalCredits() {
    getFields().put("totalCredits", null);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("... on Human {");
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
