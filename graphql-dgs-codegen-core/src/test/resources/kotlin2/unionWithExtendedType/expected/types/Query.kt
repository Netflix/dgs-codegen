package kotlin2.unionWithExtendedType.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> List<SearchResult?>? = searchDefault,
) {
  private val _search: () -> List<SearchResult?>? = search

  public val search: List<SearchResult?>?
    get() = _search.invoke()

  public companion object {
    private val searchDefault: () -> List<SearchResult?>? = 
        { throw IllegalStateException("Field `search` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var search: () -> List<SearchResult?>? = searchDefault

    @JsonProperty("search")
    public fun withSearch(search: List<SearchResult?>?): Builder = this.apply {
      this.search = { search }
    }

    public fun build() = Query(
      search = search,
    )
  }
}
