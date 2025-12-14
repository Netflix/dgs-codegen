package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassDocs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

/**
 * Movies are fun to watch.
 * They also work well as examples in GraphQL.
 */
public class Movie {
  private String title;

  public Movie() {
  }

  public Movie(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return "Movie{title='" + title + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Movie that = (Movie) o;
    return Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String title;

    public Movie build() {
      Movie result = new Movie();
      result.title = this.title;
      return result;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }
  }
}
