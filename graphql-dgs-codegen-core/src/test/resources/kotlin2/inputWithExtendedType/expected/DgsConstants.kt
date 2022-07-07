package kotlin2.inputWithExtendedType.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val Movies: String = "movies"

    public object MOVIES_INPUT_ARGUMENT {
      public const val Filter: String = "filter"
    }
  }

  public object MOVIEFILTER {
    public const val TYPE_NAME: String = "MovieFilter"

    public const val Genre: String = "genre"

    public const val ReleaseYear: String = "releaseYear"
  }
}
