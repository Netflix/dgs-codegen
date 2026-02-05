package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithMappedTypes.expected;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Entity = "entity";

    public static final String EntityConnection = "entityConnection";
  }

  public static class ENTITY {
    public static final String TYPE_NAME = "Entity";

    public static final String Long = "long";

    public static final String DateTime = "dateTime";
  }

  public static class ENTITYCONNECTION {
    public static final String TYPE_NAME = "EntityConnection";

    public static final String PageInfo = "pageInfo";

    public static final String Edges = "edges";
  }

  public static class ENTITYEDGE {
    public static final String TYPE_NAME = "EntityEdge";

    public static final String Cursor = "cursor";

    public static final String Node = "node";
  }

  public static class PAGEINFO {
    public static final String TYPE_NAME = "PageInfo";

    public static final String StartCursor = "startCursor";

    public static final String EndCursor = "endCursor";

    public static final String HasNextPage = "hasNextPage";

    public static final String HasPreviousPage = "hasPreviousPage";
  }
}
