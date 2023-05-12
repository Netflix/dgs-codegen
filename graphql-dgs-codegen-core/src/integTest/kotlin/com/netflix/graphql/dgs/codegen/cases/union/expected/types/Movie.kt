package com.netflix.graphql.dgs.codegen.cases.union.expected.types

import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
import java.lang.IllegalStateException
import kotlin.String
import kotlin.jvm.JvmName

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonDeserialize(builder = Movie.Builder::class)
public class Movie(
    title: () -> String? = titleDefault
) : SearchResult {
    private val _title: () -> String? = title

    @get:JvmName("getTitle")
    public val title: String?
        get() = _title.invoke()

    public companion object {
        private val titleDefault: () -> String? = { throw IllegalStateException("Field `title` was not requested") }
    }

    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
        private var title: () -> String? = titleDefault

        @JsonProperty("title")
        public fun withTitle(title: String?): Builder = this.apply {
            this.title = { title }
        }

        public fun build() = Movie(
            title = title
        )
    }
}
