package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MovieFilter {
  /**
   * Starts-with filter
   */
  private String titleFilter;

  public MovieFilter() {
  }

  public MovieFilter(String titleFilter) {
    this.titleFilter = titleFilter;
  }

  /**
   * Starts-with filter
   */
  public String getTitleFilter() {
    return titleFilter;
  }

  public void setTitleFilter(String titleFilter) {
    this.titleFilter = titleFilter;
  }

  @Override
  public String toString() {
    return "MovieFilter{titleFilter='" + titleFilter + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MovieFilter that = (MovieFilter) o;
    return Objects.equals(titleFilter, that.titleFilter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(titleFilter);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    /**
     * Starts-with filter
     */
    private String titleFilter;

    public MovieFilter build() {
      MovieFilter result = new MovieFilter();
      result.titleFilter = this.titleFilter;
      return result;
    }

    /**
     * Starts-with filter
     */
    public Builder titleFilter(String titleFilter) {
      this.titleFilter = titleFilter;
      return this;
    }
  }
}
