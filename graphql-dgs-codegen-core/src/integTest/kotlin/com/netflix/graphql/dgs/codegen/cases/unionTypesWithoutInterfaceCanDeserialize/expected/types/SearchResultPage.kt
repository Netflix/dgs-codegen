package com.netflix.graphql.dgs.codegen.cases.unionTypesWithoutInterfaceCanDeserialize.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.collections.List
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = SearchResultPage.Builder::class)
public class SearchResultPage(
    items: () -> List<SearchResult?>? = itemsDefault
) {
    private val _items: () -> List<SearchResult?>? = items

    @get:JvmName("getItems")
    public val items: List<SearchResult?>?
        get() = _items.invoke()

    public companion object {
        private val itemsDefault: () -> List<SearchResult?>? = { throw IllegalStateException("Field `items` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var items: () -> List<SearchResult?>? = itemsDefault

        @JsonProperty("items")
        public fun withItems(items: List<SearchResult?>?): Builder = this.apply {
            this.items = { items }
        }

        public fun build(): SearchResultPage = SearchResultPage(
            items = items
        )
    }
}
