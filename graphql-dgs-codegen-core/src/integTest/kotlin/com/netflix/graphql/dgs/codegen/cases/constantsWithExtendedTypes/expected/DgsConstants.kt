package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedTypes.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Email: String = "email"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }
}
