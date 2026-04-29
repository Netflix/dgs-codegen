package com.netflix.graphql.dgs.codegen.cases.dataClassWithReservedWord.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object SAMPLETYPE {
    public const val TYPE_NAME: String = "SampleType"

    public const val Return: String = "return"
  }

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  @Generated
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Info: String = "info"

    public const val Interface: String = "interface"

    @Generated
    public object INFO_INPUT_ARGUMENT {
      public const val Package: String = "package"
    }
  }
}
