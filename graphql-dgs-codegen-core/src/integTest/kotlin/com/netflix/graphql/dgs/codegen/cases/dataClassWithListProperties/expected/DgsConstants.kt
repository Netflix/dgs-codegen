package com.netflix.graphql.dgs.codegen.cases.dataClassWithListProperties.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Name: String = "name"

    public const val Email: String = "email"
  }
}
