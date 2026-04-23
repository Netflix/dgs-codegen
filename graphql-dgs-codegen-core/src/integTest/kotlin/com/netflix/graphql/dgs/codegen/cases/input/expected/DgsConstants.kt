package com.netflix.graphql.dgs.codegen.cases.input.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Movies: String = "movies"

    @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
    @Generated
    public object MOVIES_INPUT_ARGUMENT {
      public const val Filter: String = "filter"
    }
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val Genre: String = "genre"
  }
}
