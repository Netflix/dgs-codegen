package kotlin2.projectionWithPrimitives.expected

import kotlin.String
import kotlin2.projectionWithPrimitives.expected.client.QueryProjection

public object DgsClient {
  public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String {
    val projection = QueryProjection()
    _projection.invoke(projection)
    return "query ${projection.asQuery()}"
  }
}
