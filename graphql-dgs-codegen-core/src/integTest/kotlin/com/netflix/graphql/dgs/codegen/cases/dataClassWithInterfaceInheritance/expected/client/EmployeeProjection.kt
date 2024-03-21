package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class EmployeeProjection : GraphQLProjection() {
    public val firstname: EmployeeProjection
        get() {
            field("firstname")
            return this
        }

    public val lastname: EmployeeProjection
        get() {
            field("lastname")
            return this
        }

    public val company: EmployeeProjection
        get() {
            field("company")
            return this
        }

    public fun onTalent(_projection: TalentProjection.() -> TalentProjection): EmployeeProjection {
        fragment("Talent", TalentProjection(), _projection)
        return this
    }
}
