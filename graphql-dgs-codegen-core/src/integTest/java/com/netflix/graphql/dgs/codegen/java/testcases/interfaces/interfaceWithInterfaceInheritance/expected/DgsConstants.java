package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithInterfaceInheritance.expected;

import java.lang.String;

@Generated
public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  @Generated
  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Fruits = "fruits";
  }

  @Generated
  public static class SEED {
    public static final String TYPE_NAME = "Seed";

    public static final String Name = "name";
  }

  @Generated
  public static class FRUIT {
    public static final String TYPE_NAME = "Fruit";

    public static final String Seeds = "seeds";
  }

  @Generated
  public static class STONEFRUIT {
    public static final String TYPE_NAME = "StoneFruit";

    public static final String Seeds = "seeds";

    public static final String Fuzzy = "fuzzy";
  }
}
