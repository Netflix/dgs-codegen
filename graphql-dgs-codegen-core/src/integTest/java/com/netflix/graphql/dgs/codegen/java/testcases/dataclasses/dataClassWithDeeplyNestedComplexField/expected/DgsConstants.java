package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeeplyNestedComplexField.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Cars = "cars";
  }

  public static class CAR {
    public static final String TYPE_NAME = "Car";

    public static final String Make = "make";

    public static final String Model = "model";

    public static final String Engine = "engine";
  }

  public static class ENGINE {
    public static final String TYPE_NAME = "Engine";

    public static final String Type = "type";

    public static final String Bhp = "bhp";

    public static final String Size = "size";

    public static final String Performance = "performance";
  }

  public static class PERFORMANCE {
    public static final String TYPE_NAME = "Performance";

    public static final String ZeroToSixty = "zeroToSixty";

    public static final String QuarterMile = "quarterMile";
  }
}
