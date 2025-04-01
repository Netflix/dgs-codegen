package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;
import java.lang.String;
import java.util.ArrayList;

public class PeopleProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public PeopleProjectionRoot() {
    super(null, null, java.util.Optional.of("Person"));
  }

  public PeopleProjectionRoot<PARENT, ROOT> __typename() {
    getFields().put("__typename", null);
    return this;
  }

  public StringProjection<PeopleProjectionRoot<PARENT, ROOT>, PeopleProjectionRoot<PARENT, ROOT>> info(
      String package1) {
    StringProjection<PeopleProjectionRoot<PARENT, ROOT>, PeopleProjectionRoot<PARENT, ROOT>> projection = new StringProjection<>(this, this);    
    getFields().put("info", projection);
    getInputArguments().computeIfAbsent("info", k -> new ArrayList<>());                      
    InputArgument package1Arg = new InputArgument("package1", package1);
    getInputArguments().get("info").add(package1Arg);
    return projection;
  }

  public PeopleProjectionRoot<PARENT, ROOT> info() {
    getFields().put("info", null);
    return this;
  }

  public PeopleProjectionRoot<PARENT, ROOT> _interface() {
    getFields().put("interface", null);
    return this;
  }
}
