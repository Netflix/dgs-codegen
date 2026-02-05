package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithInterfaceInheritance.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String People = "people";
  }

  public static class TALENT {
    public static final String TYPE_NAME = "Talent";

    public static final String Firstname = "firstname";

    public static final String Lastname = "lastname";

    public static final String Company = "company";

    public static final String ImdbProfile = "imdbProfile";
  }

  public static class PERSON {
    public static final String TYPE_NAME = "Person";

    public static final String Firstname = "firstname";

    public static final String Lastname = "lastname";
  }

  public static class EMPLOYEE {
    public static final String TYPE_NAME = "Employee";

    public static final String Firstname = "firstname";

    public static final String Lastname = "lastname";

    public static final String Company = "company";
  }
}
