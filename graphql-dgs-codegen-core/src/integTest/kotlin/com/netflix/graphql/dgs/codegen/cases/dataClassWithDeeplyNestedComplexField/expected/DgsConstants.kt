package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeeplyNestedComplexField.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Cars: String = "cars"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object CAR {
    public const val TYPE_NAME: String = "Car"

    public const val Make: String = "make"

    public const val Model: String = "model"

    public const val Engine: String = "engine"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object ENGINE {
    public const val TYPE_NAME: String = "Engine"

    public const val Type: String = "type"

    public const val Bhp: String = "bhp"

    public const val Size: String = "size"

    public const val Performance: String = "performance"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object PERFORMANCE {
    public const val TYPE_NAME: String = "Performance"

    public const val ZeroToSixty: String = "zeroToSixty"

    public const val QuarterMile: String = "quarterMile"
  }
}
