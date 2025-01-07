package com.netflix.graphql.dgs.codegen.cases.dataClassWithPrefix.expected.types
  
  import com.fasterxml.jackson.`annotation`.JsonIgnoreProperties
  import com.fasterxml.jackson.`annotation`.JsonProperty
  import com.fasterxml.jackson.`annotation`.JsonTypeInfo
  import com.fasterxml.jackson.databind.`annotation`.JsonDeserialize
  import com.fasterxml.jackson.databind.`annotation`.JsonPOJOBuilder
  import java.lang.IllegalStateException
  import kotlin.jvm.JvmName
  
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  @JsonDeserialize(builder = DgsQuery.Builder::class)
  public class DgsQuery(
    search: () -> DgsMovie? = searchDefault,
  ) {
    private val __search: () -> DgsMovie? = search
  
    @get:JvmName("getSearch")
    public val search: DgsMovie?
      get() = __search.invoke()
  
    public companion object {
      private val searchDefault: () -> DgsMovie? = 
          { throw IllegalStateException("Field `search` was not requested") }
    }
  
    @JsonPOJOBuilder
    @JsonIgnoreProperties("__typename")
    public class Builder {
      private var search: () -> DgsMovie? = searchDefault
  
      @JsonProperty("search")
      public fun withSearch(search: DgsMovie?): Builder = this.apply {
        this.search = { search }
      }
  
      public fun build(): DgsQuery = DgsQuery(
        search = search,
      )
    }
  }