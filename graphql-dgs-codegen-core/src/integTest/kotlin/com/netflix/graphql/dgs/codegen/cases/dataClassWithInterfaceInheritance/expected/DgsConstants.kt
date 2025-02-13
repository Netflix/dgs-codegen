package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  public object TALENT {
    public const val TYPE_NAME: String = "Talent"

    public const val Company: String = "company"

    public const val Firstname: String = "firstname"

    public const val ImdbProfile: String = "imdbProfile"

    public const val Lastname: String = "lastname"
  }

  public object EMPLOYEE {
    public const val TYPE_NAME: String = "Employee"

    public const val Company: String = "company"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }
}
