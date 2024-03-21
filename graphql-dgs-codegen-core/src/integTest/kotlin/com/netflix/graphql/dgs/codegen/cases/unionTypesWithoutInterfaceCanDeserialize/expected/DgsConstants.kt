package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected

import kotlin.String

public object DgsConstants {
    public const val QUERY_TYPE: String = "Query"

    public object QUERY {
        public const val TYPE_NAME: String = "Query"

        public const val Search: String = "search"

        public object SEARCH_INPUT_ARGUMENT {
            public const val Text: String = "text"
        }
    }

    public object HUMAN {
        public const val TYPE_NAME: String = "Human"

        public const val Id: String = "id"

        public const val Name: String = "name"

        public const val TotalCredits: String = "totalCredits"
    }

    public object DROID {
        public const val TYPE_NAME: String = "Droid"

        public const val Id: String = "id"

        public const val Name: String = "name"

        public const val PrimaryFunction: String = "primaryFunction"
    }

    public object SEARCHRESULTPAGE {
        public const val TYPE_NAME: String = "SearchResultPage"

        public const val Items: String = "items"
    }

    public object SEARCHRESULT {
        public const val TYPE_NAME: String = "SearchResult"
    }
}
