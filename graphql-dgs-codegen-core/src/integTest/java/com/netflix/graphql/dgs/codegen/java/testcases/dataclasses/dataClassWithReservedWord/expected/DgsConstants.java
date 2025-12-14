package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithReservedWord.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class SAMPLETYPE {
    public static final String TYPE_NAME = "SampleType";

    public static final String Return = "return";
  }

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String People = "people";
  }

  public static class PERSON {
    public static final String TYPE_NAME = "Person";

    public static final String Info = "info";

    public static final String Interface = "interface";

    public static class INFO_INPUT_ARGUMENT {
      public static final String Package = "package";
    }
  }
}
