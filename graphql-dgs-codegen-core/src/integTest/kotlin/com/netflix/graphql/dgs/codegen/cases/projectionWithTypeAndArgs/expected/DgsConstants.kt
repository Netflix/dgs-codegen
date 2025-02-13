package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected

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

    public const val Person: String = "person"

    public object PERSON_INPUT_ARGUMENT {
      public const val A1: String = "a1"

      public const val A2: String = "a2"

      public const val A3: String = "a3"
    }
  }

  public object I {
    public const val TYPE_NAME: String = "I"

    public const val Arg: String = "arg"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"
  }
}
