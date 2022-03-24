package kotlin2.constantsWithExtendedInterface.expected

import kotlin.String
import kotlin2.constantsWithExtendedInterface.expected.client.QueryProjection

public object DgsClient {
  public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String {
    val projection = QueryProjection()
    _projection.invoke(projection)
    return "query ${projection.asQuery()}"
  }
}
