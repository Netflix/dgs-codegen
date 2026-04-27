package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected;

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
      public static final String MovieFilter = "movieFilter";
    }
  }

  @Generated
  public static class MOVIE {
    public static final String TYPE_NAME = "Movie";

    public static final String Title = "title";
  }

  @Generated
  public static class MOVIEFILTER {
    public static final String TYPE_NAME = "MovieFilter";

    public static final String TitleFilter = "titleFilter";
  }
}
