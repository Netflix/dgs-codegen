package com.netflix.graphql.dgs.codegen.cases.dataClassWithInterfaceInheritance.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class TalentProjection : GraphQLProjection() {
  public val firstname: TalentProjection
    get() {
      field("firstname")
      return this
    }

  public val lastname: TalentProjection
    get() {
      field("lastname")
      return this
    }

  public val company: TalentProjection
    get() {
      field("company")
      return this
    }

  public val imdbProfile: TalentProjection
    get() {
      field("imdbProfile")
      return this
    }
}
