package kotlin2.dataClassWithDeclaredScalars.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.Boolean
import kotlin.String

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = PageInfo.Builder::class)
public class PageInfo(
  startCursor: () -> String? = startCursorDefault,
  endCursor: () -> String? = endCursorDefault,
  hasNextPage: () -> Boolean = hasNextPageDefault,
  hasPreviousPage: () -> Boolean = hasPreviousPageDefault,
) {
  private val _startCursor: () -> String? = startCursor

  private val _endCursor: () -> String? = endCursor

  private val _hasNextPage: () -> Boolean = hasNextPage

  private val _hasPreviousPage: () -> Boolean = hasPreviousPage

  public val startCursor: String?
    get() = _startCursor.invoke()

  public val endCursor: String?
    get() = _endCursor.invoke()

  public val hasNextPage: Boolean
    get() = _hasNextPage.invoke()

  public val hasPreviousPage: Boolean
    get() = _hasPreviousPage.invoke()

  public companion object {
    private val startCursorDefault: () -> String? = 
        { throw IllegalStateException("Field `startCursor` was not requested") }


    private val endCursorDefault: () -> String? = 
        { throw IllegalStateException("Field `endCursor` was not requested") }


    private val hasNextPageDefault: () -> Boolean = 
        { throw IllegalStateException("Field `hasNextPage` was not requested") }


    private val hasPreviousPageDefault: () -> Boolean = 
        { throw IllegalStateException("Field `hasPreviousPage` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var startCursor: () -> String? = startCursorDefault

    private var endCursor: () -> String? = endCursorDefault

    private var hasNextPage: () -> Boolean = hasNextPageDefault

    private var hasPreviousPage: () -> Boolean = hasPreviousPageDefault

    @JsonProperty("startCursor")
    public fun withStartCursor(startCursor: String?): Builder = this.apply {
      this.startCursor = { startCursor }
    }

    @JsonProperty("endCursor")
    public fun withEndCursor(endCursor: String?): Builder = this.apply {
      this.endCursor = { endCursor }
    }

    @JsonProperty("hasNextPage")
    public fun withHasNextPage(hasNextPage: Boolean): Builder = this.apply {
      this.hasNextPage = { hasNextPage }
    }

    @JsonProperty("hasPreviousPage")
    public fun withHasPreviousPage(hasPreviousPage: Boolean): Builder = this.apply {
      this.hasPreviousPage = { hasPreviousPage }
    }

    public fun build() = PageInfo(
      startCursor = startCursor,
      endCursor = endCursor,
      hasNextPage = hasNextPage,
      hasPreviousPage = hasPreviousPage,
    )
  }
}
