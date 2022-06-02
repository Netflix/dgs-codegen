package kotlin2.dataClassWithDeeplyNestedComplexField.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Cars: String = "cars"
  }

  public object CAR {
    public const val TYPE_NAME: String = "Car"

    public const val Make: String = "make"

    public const val Model: String = "model"

    public const val Engine: String = "engine"
  }

  public object ENGINE {
    public const val TYPE_NAME: String = "Engine"

    public const val Type: String = "type"

    public const val Bhp: String = "bhp"

    public const val Size: String = "size"

    public const val Performance: String = "performance"
  }

  public object PERFORMANCE {
    public const val TYPE_NAME: String = "Performance"

    public const val ZeroToSixty: String = "zeroToSixty"

    public const val QuarterMile: String = "quarterMile"
  }
}
