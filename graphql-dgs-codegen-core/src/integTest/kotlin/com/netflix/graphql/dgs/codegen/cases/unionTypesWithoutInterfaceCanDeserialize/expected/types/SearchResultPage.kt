package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize as FasterxmlJacksonDatabindAnnotationJsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder as FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
import tools.jackson.databind.`annotation`.JsonDeserialize as ToolsJacksonDatabindAnnotationJsonDeserialize
import tools.jackson.databind.`annotation`.JsonPOJOBuilder as ToolsJacksonDatabindAnnotationJsonPOJOBuilder

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@FasterxmlJacksonDatabindAnnotationJsonDeserialize(builder = SearchResultPage.Builder::class)
@ToolsJacksonDatabindAnnotationJsonDeserialize(builder = SearchResultPage.Builder::class)
public class SearchResultPage(
  items: () -> List<SearchResult?>? = itemsDefault,
) {
  private val __items: () -> List<SearchResult?>? = items

  @get:JvmName("getItems")
  public val items: List<SearchResult?>?
    get() = __items.invoke()

  public companion object {
    private val itemsDefault: () -> List<SearchResult?>? = 
        { throw IllegalStateException("Field `items` was not requested") }
  }

  @FasterxmlJacksonDatabindAnnotationJsonPOJOBuilder
  @ToolsJacksonDatabindAnnotationJsonPOJOBuilder
  @JsonIgnoreProperties("__typename")
  public class Builder {
    private var items: () -> List<SearchResult?>? = itemsDefault

    @JsonProperty("items")
    public fun withItems(items: List<SearchResult?>?): Builder = this.apply {
      this.items = { items }
    }

    public fun build(): SearchResultPage = SearchResultPage(
      items = items,
    )
  }
}
