package kotlin2.projectionWithSameDefinitions.expected

import kotlin.String
import kotlin2.projectionWithSameDefinitions.expected.client.MutationProjection
import kotlin2.projectionWithSameDefinitions.expected.client.QueryProjection
import kotlin2.projectionWithSameDefinitions.expected.client.SubscriptionProjection

public object DgsClient {
  public fun buildQuery(_projection: QueryProjection.() -> QueryProjection): String {
    val projection = QueryProjection()
    _projection.invoke(projection)
    return "query ${projection.asQuery()}"
  }

  public fun buildMutation(_projection: MutationProjection.() -> MutationProjection): String {
    val projection = MutationProjection()
    _projection.invoke(projection)
    return "mutation ${projection.asQuery()}"
  }

  public fun buildSubscription(_projection: SubscriptionProjection.() -> SubscriptionProjection):
      String {
    val projection = SubscriptionProjection()
    _projection.invoke(projection)
    return "subscription ${projection.asQuery()}"
  }
}
