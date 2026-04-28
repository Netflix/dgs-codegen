package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedQuery.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"

    public const val Friends: String = "friends"
  }

  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }
}
