package com.netflix.graphql.dgs.codegen.cases.union.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Search: String = "search"
  }

  @Generated
  public object MOVIE {
    public const val TYPE_NAME: String = "Movie"

    public const val Title: String = "title"
  }

  @Generated
  public object ACTOR {
    public const val TYPE_NAME: String = "Actor"

    public const val Name: String = "name"
  }

  @Generated
  public object SEARCHRESULT {
    public const val TYPE_NAME: String = "SearchResult"
  }
}
