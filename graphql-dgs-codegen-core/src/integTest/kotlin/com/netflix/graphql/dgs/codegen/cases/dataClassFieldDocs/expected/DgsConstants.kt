package com.netflix.graphql.dgs.codegen.cases.dataClassFieldDocs.expected

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

    @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
    @Generated
    public object SEARCH_INPUT_ARGUMENT {
      public const val MovieFilter: String = "movieFilter"
    }
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object MOVIE {
    public const val TYPE_NAME: String = "Movie"

    public const val Title: String = "title"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val TitleFilter: String = "titleFilter"
  }
}
