package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassDocs.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Search = "search";

    public static class SEARCH_INPUT_ARGUMENT {
      public static final String MovieFilter = "movieFilter";
    }
  }

  public static class MOVIE {
    public static final String TYPE_NAME = "Movie";

    public static final String Title = "title";
  }

  public static class MOVIEFILTER {
    public static final String TYPE_NAME = "MovieFilter";

    public static final String TitleFilter = "titleFilter";
  }
}
