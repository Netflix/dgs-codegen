package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Fruits: String = "fruits"
  }

  @Generated
  public object SEED {
    public const val TYPE_NAME: String = "Seed"

    public const val Name: String = "name"
  }

  @Generated
  public object FRUIT {
    public const val TYPE_NAME: String = "Fruit"

    public const val Seeds: String = "seeds"
  }

  @Generated
  public object STONEFRUIT {
    public const val TYPE_NAME: String = "StoneFruit"

    public const val Seeds: String = "seeds"

    public const val Fuzzy: String = "fuzzy"
  }
}
