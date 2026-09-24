package com.netflix.graphql.dgs.codegen.java.testcases.constants.constantsForInputTypes.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String People = "people";

    public static class PEOPLE_INPUT_ARGUMENT {
      public static final String Filter = "filter";
    }
  }

  public static class PERSON {
    public static final String TYPE_NAME = "Person";

    public static final String Firstname = "firstname";

    public static final String Lastname = "lastname";
  }

  public static class PERSONFILTER {
    public static final String TYPE_NAME = "PersonFilter";

    public static final String Email = "email";
  }
}
