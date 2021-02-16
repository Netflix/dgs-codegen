package com.netflix.graphql.dgs.codegen.tests.generated;

import java.lang.String;

public class DgsConstants {
  public static final String QUERY_TYPE = "Query";

  public static class QUERY {
    public static final String TYPE_NAME = "Query";

    public static final String Workshop = "workshop";
  }

  public static class WORKSHOP {
    public static final String TYPE_NAME = "Workshop";

    public static final String Reviews = "reviews";

    public static final String Assets = "assets";
  }

  public static class REVIEWCONNECTION {
    public static final String TYPE_NAME = "ReviewConnection";

    public static final String Edges = "edges";
  }

  public static class REVIEWEDGE {
    public static final String TYPE_NAME = "ReviewEdge";

    public static final String Node = "node";
  }

  public static class ASSET {
    public static final String TYPE_NAME = "Asset";

    public static final String Reviews = "reviews";
  }
}
