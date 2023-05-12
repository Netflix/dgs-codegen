package com.netflix.graphql.dgs.codegen.cases.projectionWithTypeAndArgs.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class PersonProjection : GraphQLProjection() {
    public val firstname: PersonProjection
        get() {
            field("firstname")
            return this
        }

    public fun onEmployee(_projection: EmployeeProjection.() -> EmployeeProjection):
        PersonProjection {
        fragment("Employee", EmployeeProjection(), _projection)
        return this
    }
}
