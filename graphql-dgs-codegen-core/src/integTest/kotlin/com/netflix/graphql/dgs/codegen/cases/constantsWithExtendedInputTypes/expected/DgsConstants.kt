package com.netflix.graphql.dgs.codegen.cases.constantsWithExtendedInputTypes.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"

    @Generated
    public object PEOPLE_INPUT_ARGUMENT {
      public const val Filter: String = "filter"
    }
  }

  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  @Generated
  public object PERSONFILTER {
    public const val TYPE_NAME: String = "PersonFilter"

    public const val Email: String = "email"

    public const val BirthYear: String = "birthYear"
  }
}
