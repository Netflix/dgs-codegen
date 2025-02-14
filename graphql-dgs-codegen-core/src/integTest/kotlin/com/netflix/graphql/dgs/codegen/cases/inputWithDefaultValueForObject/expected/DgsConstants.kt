package com.netflix.graphql.dgs.codegen.cases.inputWithDefaultValueForObject.expected

import kotlin.String

public object DgsConstants {
  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Name: String = "name"

    public const val Age: String = "age"

    public const val Car: String = "car"
  }

  public object CAR {
    public const val TYPE_NAME: String = "Car"

    public const val Brand: String = "brand"
  }

  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val Director: String = "director"
  }
}
