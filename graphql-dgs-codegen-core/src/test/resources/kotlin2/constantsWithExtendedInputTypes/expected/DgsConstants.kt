package kotlin2.constantsWithExtendedInputTypes.expected

import kotlin.String

public object DgsConstants {
  public const val QUERY_TYPE: String = "Query"

  public object QUERY {
    public const val TYPE_NAME: String = "Query"

    public const val People: String = "people"
  }

  public object PERSON {
    public const val TYPE_NAME: String = "Person"

    public const val Firstname: String = "firstname"

    public const val Lastname: String = "lastname"
  }

  public object PERSONFILTER {
    public const val TYPE_NAME: String = "PersonFilter"

    public const val Email: String = "email"

    public const val BirthYear: String = "birthYear"
  }
}
