package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected;

import java.lang.String;

@Generated
public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  @Generated
  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Search = "search";

    @Generated
    public static class SEARCH_INPUT_ARGUMENT {
      public static final String Text = "text";
    }
  }

  @Generated
  public static class HUMAN {
    public static final String TYPE_NAME = "Human";

    public static final String Id = "id";

    public static final String Name = "name";

    public static final String TotalCredits = "totalCredits";
  }

  @Generated
  public static class DROID {
    public static final String TYPE_NAME = "Droid";

    public static final String Id = "id";

    public static final String Name = "name";

    public static final String PrimaryFunction = "primaryFunction";
  }

  @Generated
  public static class SEARCHRESULTPAGE {
    public static final String TYPE_NAME = "SearchResultPage";

    public static final String Items = "items";
  }
}
