package com.netflix.graphql.dgs.codegen.cases.dataClassWithDeclaredScalars.expected

import kotlin.String

@Generated
public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  @Generated
  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Entity: String = "entity"

    public const val EntityConnection: String = "entityConnection"
  }

  @Generated
  public object ENTITY {
    public const val TYPE_NAME: String = "Entity"

    public const val Long: String = "long"

    public const val DateTime: String = "dateTime"
  }

  @Generated
  public object ENTITYCONNECTION {
    public const val TYPE_NAME: String = "EntityConnection"

    public const val PageInfo: String = "pageInfo"

    public const val Edges: String = "edges"
  }

  @Generated
  public object ENTITYEDGE {
    public const val TYPE_NAME: String = "EntityEdge"

    public const val Cursor: String = "cursor"

    public const val Node: String = "node"
  }

  @Generated
  public object PAGEINFO {
    public const val TYPE_NAME: String = "PageInfo"

    public const val StartCursor: String = "startCursor"

    public const val EndCursor: String = "endCursor"

    public const val HasNextPage: String = "hasNextPage"

    public const val HasPreviousPage: String = "hasPreviousPage"
  }
}
