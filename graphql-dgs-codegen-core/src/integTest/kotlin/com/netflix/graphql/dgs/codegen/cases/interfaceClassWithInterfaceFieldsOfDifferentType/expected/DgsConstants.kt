package com.netflix.graphql.dgs.codegen.cases.interfaceClassWithInterfaceFieldsOfDifferentType.expected

import kotlin.String

public object DgsConstants {
    public object VEGETARIAN {
        public const val TYPE_NAME: String = "Vegetarian"

        public const val Calories: String = "calories"

        public const val Vegetables: String = "vegetables"
    }

    public object DOG {
        public const val TYPE_NAME: String = "Dog"

        public const val Name: String = "name"

        public const val Diet: String = "diet"
    }

    public object PET {
        public const val TYPE_NAME: String = "Pet"

        public const val Name: String = "name"

        public const val Diet: String = "diet"
    }

    public object DIET {
        public const val TYPE_NAME: String = "Diet"

        public const val Calories: String = "calories"
    }
}
