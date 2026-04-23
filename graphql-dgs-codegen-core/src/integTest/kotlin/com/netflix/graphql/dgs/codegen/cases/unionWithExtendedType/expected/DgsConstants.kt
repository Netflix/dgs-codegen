package com.netflix.graphql.dgs.codegen.cases.unionWithExtendedType.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Search: String = "search"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object MOVIE {
    public const val TYPE_NAME: String = "Movie"

    public const val Title: String = "title"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object ACTOR {
    public const val TYPE_NAME: String = "Actor"

    public const val Name: String = "name"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object RATING {
    public const val TYPE_NAME: String = "Rating"

    public const val Stars: String = "stars"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object SEARCHRESULT {
    public const val TYPE_NAME: String = "SearchResult"
  }
}
