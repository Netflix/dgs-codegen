package com.netflix.graphql.dgs.codegen.cases.projectionWithUnion.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object EMPLOYEE {
    public const val TYPE_NAME: String = "Employee"

    public const val Company: String = "company"

    public const val Firstname: String = "firstname"
  }

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val U: String = "u"

    public const val Us: String = "us"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"
  }

  public object U {
    public const val TYPE_NAME: String = "U"
  }
}
