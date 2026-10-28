package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.types;

import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.Generated;
import java.lang.Boolean;
import java.util.List;

@Generated
public interface StoneFruit extends Fruit {
  List<Seed> getSeeds();

  void setSeeds(List<Seed> seeds);

  Boolean getFuzzy();

  void setFuzzy(Boolean fuzzy);
}
