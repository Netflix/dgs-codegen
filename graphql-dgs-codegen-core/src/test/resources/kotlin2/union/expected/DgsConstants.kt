package kotlin2.union.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Search: String = "search"
  }

  public object MOVIE {
    public const val TYPE_NAME: String = "Movie"

    public const val Title: String = "title"
  }

  public object ACTOR {
    public const val TYPE_NAME: String = "Actor"

    public const val Name: String = "name"
  }

  public object SEARCHRESULT {
    public const val TYPE_NAME: String = "SearchResult"
  }
}
