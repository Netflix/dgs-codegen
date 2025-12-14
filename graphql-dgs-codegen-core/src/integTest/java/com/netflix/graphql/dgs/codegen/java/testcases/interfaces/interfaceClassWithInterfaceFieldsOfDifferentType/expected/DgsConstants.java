package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFieldsOfDifferentType.expected;

import java.lang.String;

public class DgsConstants {
  public static class VEGETARIAN {
    public static final String TYPE_NAME = "Vegetarian";

    public static final String Calories = "calories";

    public static final String Vegetables = "vegetables";
  }

  public static class DOG {
    public static final String TYPE_NAME = "Dog";

    public static final String Name = "name";

    public static final String Diet = "diet";
  }

  public static class PET {
    public static final String TYPE_NAME = "Pet";

    public static final String Name = "name";

    public static final String Diet = "diet";
  }

  public static class DIET {
    public static final String TYPE_NAME = "Diet";

    public static final String Calories = "calories";
  }
}
