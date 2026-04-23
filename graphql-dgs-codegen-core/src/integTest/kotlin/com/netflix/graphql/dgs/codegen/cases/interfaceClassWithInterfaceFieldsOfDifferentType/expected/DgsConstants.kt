package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected

import kotlin.String

@jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
@Generated
public object DgsConstants {
  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object VEGETARIAN {
    public const val TYPE_NAME: String = "Vegetarian"

    public const val Calories: String = "calories"

    public const val Vegetables: String = "vegetables"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object DOG {
    public const val TYPE_NAME: String = "Dog"

    public const val Name: String = "name"

    public const val Diet: String = "diet"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object PET {
    public const val TYPE_NAME: String = "Pet"

    public const val Name: String = "name"

    public const val Diet: String = "diet"
  }

  @jakarta.`annotation`.Generated(value = ["com.netflix.graphql.dgs.codegen.CodeGen"])
  @Generated
  public object DIET {
    public const val TYPE_NAME: String = "Diet"

    public const val Calories: String = "calories"
  }
}
