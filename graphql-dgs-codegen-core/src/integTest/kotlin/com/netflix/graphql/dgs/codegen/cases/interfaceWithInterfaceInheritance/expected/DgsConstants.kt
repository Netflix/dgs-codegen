package com.netflix.graphql.dgs.codegen.cases.interfaceWithInterfaceInheritance.expected

import kotlin.String

public object DgsConstants {
    public const val QUERY_TYPE: String = "Query"

    public object QUERY {
        public const val TYPE_NAME: String = "Query"

        public const val Fruits: String = "fruits"
    }

    public object SEED {
        public const val TYPE_NAME: String = "Seed"

        public const val Name: String = "name"
    }

    public object FRUIT {
        public const val TYPE_NAME: String = "Fruit"

        public const val Seeds: String = "seeds"
    }

    public object STONEFRUIT {
        public const val TYPE_NAME: String = "StoneFruit"

        public const val Seeds: String = "seeds"

        public const val Fuzzy: String = "fuzzy"
    }
}
