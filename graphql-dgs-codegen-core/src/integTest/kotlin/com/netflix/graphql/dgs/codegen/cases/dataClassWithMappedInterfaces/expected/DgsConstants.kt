package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected

import kotlin.String

public object DgsConstants {
    public const val QUERY_TYPE: String = "Query"

    public object QUERY {
        public const val TYPE_NAME: String = "Query"

        public const val Products: String = "products"
    }

    public object PRODUCT {
        public const val TYPE_NAME: String = "Product"

        public const val Id: String = "id"
    }

    public object NODE {
        public const val TYPE_NAME: String = "Node"

        public const val Id: String = "id"
    }

    public object ENTITY {
        public const val TYPE_NAME: String = "Entity"

        public const val Id: String = "id"
    }
}
