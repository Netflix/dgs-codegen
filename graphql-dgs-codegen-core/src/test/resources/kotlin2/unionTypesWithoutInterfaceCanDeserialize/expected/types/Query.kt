package kotlin2.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Query.Builder::class)
public class Query(
  search: () -> SearchResultPage? = searchDefault,
) {
  private val _search: () -> SearchResultPage? = search

  public val search: SearchResultPage?
    get() = _search.invoke()

  public companion object {
    private val searchDefault: () -> SearchResultPage? = 
        { throw IllegalStateException("Field `search` was not requested") }

  }

  @JsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var search: () -> SearchResultPage? = searchDefault

    @JsonProperty("search")
    public fun withSearch(search: SearchResultPage?): Builder = this.apply {
      this.search = { search }
    }

    public fun build() = Query(
      search = search,
    )
  }
}
