package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object TALENT {
    public const val TYPE_NAME: String = "Talent"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"

    public const val Company: String = "company"

    public const val ImdbProfile: String = "imdbProfile"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object EMPLOYEE {
    public const val TYPE_NAME: String = "Employee"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"

    public const val Company: String = "company"
  }
}
