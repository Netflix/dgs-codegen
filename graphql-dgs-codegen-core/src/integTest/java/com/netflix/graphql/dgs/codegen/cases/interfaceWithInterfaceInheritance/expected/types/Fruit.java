package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected.types;

import java.util.List;

public interface Fruit {
  List<Seed> getSeeds();

  void setSeeds(List<Seed> seeds);
}
