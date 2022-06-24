package kotlin2.projectionWithSameDefinitions.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public const val Mutation_TYPE: String = "Mutation"

  public const val Subscription_TYPE: String = "Subscription"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Shows: String = "shows"
  }

  public object SUBSCRIPTION {
    public const val TYPE_NAME: String = "Subscription"

    public const val Shows: String = "shows"
  }

  public object MUTATION {
    public const val TYPE_NAME: String = "Mutation"

    public const val Shows: String = "shows"
  }

  public object SHOW {
    public const val TYPE_NAME: String = "Show"

    public const val Id: String = "id"

    public const val Title: String = "title"
  }
}
