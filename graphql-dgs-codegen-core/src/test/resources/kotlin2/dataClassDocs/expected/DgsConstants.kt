package kotlin2.dataClassDocs.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Search: String = "search"

    public object SEARCH_INPUT_ARGUMENT {
      public const val MovieFilter: String = "movieFilter"
    }
  }

  public object MOVIE {
    public const val TYPE_NAME: String = "Movie"

    public const val Title: String = "title"
  }

  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val TitleFilter: String = "titleFilter"
  }
}
