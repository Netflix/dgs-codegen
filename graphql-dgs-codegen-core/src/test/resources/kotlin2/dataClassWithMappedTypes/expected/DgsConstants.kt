package kotlin2.dataClassWithMappedTypes.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Entity: String = "entity"

    public const val EntityConnection: String = "entityConnection"
  }

  public object ENTITY {
    public const val TYPE_NAME: String = "Entity"

    public const val Long: String = "long"

    public const val DateTime: String = "dateTime"
  }

  public object ENTITYCONNECTION {
    public const val TYPE_NAME: String = "EntityConnection"

    public const val PageInfo: String = "pageInfo"

    public const val Edges: String = "edges"
  }

  public object ENTITYEDGE {
    public const val TYPE_NAME: String = "EntityEdge"

    public const val Cursor: String = "cursor"

    public const val Node: String = "node"
  }

  public object PAGEINFO {
    public const val TYPE_NAME: String = "PageInfo"

    public const val StartCursor: String = "startCursor"

    public const val EndCursor: String = "endCursor"

    public const val HasNextPage: String = "hasNextPage"

    public const val HasPreviousPage: String = "hasPreviousPage"
  }
}
