package com.netflix.graphql.dgs.codegen.cases.dataClassWithNonNullableListOfNullableValues.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Name: String = "name"

    public const val Email: String = "email"
  }
}
