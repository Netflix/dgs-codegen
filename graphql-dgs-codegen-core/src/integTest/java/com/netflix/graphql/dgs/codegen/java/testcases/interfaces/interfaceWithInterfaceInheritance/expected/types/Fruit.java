package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.types;

import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected.Generated;
import java.util.List;

@Generated
public interface Fruit {
  List<Seed> getSeeds();

  void setSeeds(List<Seed> seeds);
}
