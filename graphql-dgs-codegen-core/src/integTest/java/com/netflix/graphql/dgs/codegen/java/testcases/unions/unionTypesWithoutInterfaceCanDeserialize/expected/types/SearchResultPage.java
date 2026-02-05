package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionTypesWithoutInterfaceCanDeserialize.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Objects;

public class SearchResultPage {
  private List<SearchResult> items;

  public SearchResultPage() {
  }

  public SearchResultPage(List<SearchResult> items) {
    this.items = items;
  }

  public List<SearchResult> getItems() {
    return items;
  }

  public void setItems(List<SearchResult> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "SearchResultPage{items='" + items + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SearchResultPage that = (SearchResultPage) o;
    return Objects.equals(items, that.items);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private List<SearchResult> items;

    public SearchResultPage build() {
      SearchResultPage result = new SearchResultPage();
      result.items = this.items;
      return result;
    }

    public Builder items(List<SearchResult> items) {
      this.items = items;
      return this;
    }
  }
}
