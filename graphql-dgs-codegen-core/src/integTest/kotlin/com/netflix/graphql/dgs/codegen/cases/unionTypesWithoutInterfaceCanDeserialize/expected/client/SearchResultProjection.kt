package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class SearchResultProjection : GraphQLProjection() {
    public fun onHuman(_projection: HumanProjection.() -> HumanProjection): SearchResultProjection {
        fragment("Human", HumanProjection(), _projection)
        return this
    }

    public fun onDroid(_projection: DroidProjection.() -> DroidProjection): SearchResultProjection {
        fragment("Droid", DroidProjection(), _projection)
        return this
    }
}
