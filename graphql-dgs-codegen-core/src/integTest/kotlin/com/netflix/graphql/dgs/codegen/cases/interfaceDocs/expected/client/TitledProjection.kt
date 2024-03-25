package com.netflix.graphql.dgs.codegen.cases.interfaceDocs.expected.client

import com.netflix.graphql.dgs.codegen.GraphQLProjection

public class TitledProjection : GraphQLProjection() {
  public val title: TitledProjection
    get() {
      field("title")
      return this
    }
}
