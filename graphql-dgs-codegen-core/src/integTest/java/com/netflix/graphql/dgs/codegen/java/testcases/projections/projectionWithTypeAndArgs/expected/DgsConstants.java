package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithTypeAndArgs.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Person = "person";

    public static class PERSON_INPUT_ARGUMENT {
      public static final String A1 = "a1";

      public static final String A2 = "a2";

      public static final String A3 = "a3";
    }
  }

  public static class EMPLOYEE {
    public static final String TYPE_NAME = "Employee";

    public static final String Firstname = "firstname";

    public static final String Company = "company";
  }

  public static class I {
    public static final String TYPE_NAME = "I";

    public static final String Arg = "arg";
  }

  public static class PERSON {
    public static final String TYPE_NAME = "Person";

    public static final String Firstname = "firstname";
  }
}
