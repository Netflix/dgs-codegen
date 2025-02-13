package com.netflix.graphql.dgs.codegen.cases.dataClassWithExtendedInterfaceInheritance.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object EMPLOYEE {
    public const val TYPE_NAME: String = "Employee"

    public const val Age: String = "age"

    public const val Company: String = "company"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Age: String = "age"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }
}
