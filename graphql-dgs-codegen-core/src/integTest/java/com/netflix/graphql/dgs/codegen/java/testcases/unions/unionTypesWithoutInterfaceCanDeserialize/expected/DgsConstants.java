package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Search = "search";

    public static class SEARCH_INPUT_ARGUMENT {
      public static final String Text = "text";
    }
  }

  public static class HUMAN {
    public static final String TYPE_NAME = "Human";

    public static final String Id = "id";

    public static final String Name = "name";

    public static final String TotalCredits = "totalCredits";
  }

  public static class DROID {
    public static final String TYPE_NAME = "Droid";

    public static final String Id = "id";

    public static final String Name = "name";

    public static final String PrimaryFunction = "primaryFunction";
  }

  public static class SEARCHRESULTPAGE {
    public static final String TYPE_NAME = "SearchResultPage";

    public static final String Items = "items";
  }
}
