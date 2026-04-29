package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Person: String = "person"

    @Generated
    public object PERSON_INPUT_ARGUMENT {
      public const val A1: String = "a1"

      public const val A2: String = "a2"

      public const val A3: String = "a3"
    }
  }

  @Generated
  public object EMPLOYEE {
    public const val TYPE_NAME: String = "Employee"

    public const val Firstname: String = "firstname"

    public const val Company: String = "company"
  }

  @Generated
  public object I {
    public const val TYPE_NAME: String = "I"

    public const val Arg: String = "arg"
  }

  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"
  }
}
