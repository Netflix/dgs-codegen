package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.client

import com.netflix.graphql.dgs.client.codegen.GraphQLProjection
import kotlin.String

public class QueryProjection : GraphQLProjection() {
  public fun search(text: String,
      _projection: SearchResultPageProjection.() -> SearchResultPageProjection): QueryProjection {
    val args = formatArgs("text" to text)
    project("search($args)", SearchResultPageProjection(), _projection)
    return this
  }
}
