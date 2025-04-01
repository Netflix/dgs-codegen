package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types;

import java.lang.Boolean;
import java.util.List;

public interface StoneFruit extends Fruit {
  List<Seed> getSeeds();

  void setSeeds(List<Seed> seeds);

  Boolean getFuzzy();

  void setFuzzy(Boolean fuzzy);
}
