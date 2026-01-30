package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithExtendedType.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Movies = "movies";

    public static class MOVIES_INPUT_ARGUMENT {
      public static final String Filter = "filter";
    }
  }

  public static class MOVIEFILTER {
    public static final String TYPE_NAME = "MovieFilter";

    public static final String Genre = "genre";

    public static final String ReleaseYear = "releaseYear";
  }
}
