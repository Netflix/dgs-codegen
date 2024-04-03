package com.netflix.graphql.dgs.codegen.cases.projectionWithNestedInputs.expected

import kotlin.String

public object DgsConstants {
    public const val QUERY_TYPE: String = "Query"

    public object QUERY {
        public const val TYPE_NAME: String = "Query"

        public const val Q1: String = "q1"

        public const val Q2: String = "q2"

        public object Q1_INPUT_ARGUMENT {
            public const val Arg1: String = "arg1"

            public const val Arg2: String = "arg2"
        }

        public object Q2_INPUT_ARGUMENT {
            public const val Arg1: String = "arg1"

            public const val Arg2: String = "arg2"
        }
    }

    public object I1 {
        public const val TYPE_NAME: String = "I1"

        public const val Arg1: String = "arg1"

        public const val Arg2: String = "arg2"
    }

    public object I2 {
        public const val TYPE_NAME: String = "I2"

        public const val Arg1: String = "arg1"

        public const val Arg2: String = "arg2"
    }
}
