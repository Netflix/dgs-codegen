package kotlin2.unionWithExtendedType.expected

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

  public object RATING {
    public const val TYPE_NAME: String = "Rating"

    public const val Stars: String = "stars"
  }

  public object SEARCHRESULT {
    public const val TYPE_NAME: String = "SearchResult"
  }
}
