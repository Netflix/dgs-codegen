package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object CAR {
    public const val TYPE_NAME: String = "Car"

    public const val Engine: String = "engine"

    public const val Make: String = "make"

    public const val Model: String = "model"
  }

  public object ENGINE {
    public const val TYPE_NAME: String = "Engine"

    public const val Bhp: String = "bhp"

    public const val Performance: String = "performance"

    public const val Size: String = "size"

    public const val Type: String = "type"
  }

  public object PERFORMANCE {
    public const val TYPE_NAME: String = "Performance"

    public const val QuarterMile: String = "quarterMile"

    public const val ZeroToSixty: String = "zeroToSixty"
  }

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Cars: String = "cars"
  }
}
