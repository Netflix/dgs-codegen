package com.netflix.graphql.dgs.codegen.cases.inputWithExtendedType.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Movies: String = "movies"

    @Generated
    public object MOVIES_INPUT_ARGUMENT {
      public const val Filter: String = "filter"
    }
  }

  @Generated
  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val Genre: String = "genre"

    public const val ReleaseYear: String = "releaseYear"
  }
}
