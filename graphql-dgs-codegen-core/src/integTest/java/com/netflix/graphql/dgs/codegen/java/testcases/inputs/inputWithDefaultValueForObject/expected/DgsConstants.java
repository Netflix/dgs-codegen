package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForObject.expected;

import java.lang.String;

public class DgsConstants {
  public static class PERSON {
    public static final String TYPE_NAME = "Person";

    public static final String Name = "name";

    public static final String Age = "age";

    public static final String Car = "car";
  }

  public static class CAR {
    public static final String TYPE_NAME = "Car";

    public static final String Brand = "brand";
  }

  public static class MOVIEFILTER {
    public static final String TYPE_NAME = "MovieFilter";

    public static final String Director = "director";
  }
}
