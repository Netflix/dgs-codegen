package com.netflix.graphql.dgs.codegen.java.testcases.inputs.input.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MovieFilter {
  private String genre;

  public MovieFilter() {
  }

  public MovieFilter(String genre) {
    this.genre = genre;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  @Override
  public String toString() {
    return "MovieFilter{genre='" + genre + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MovieFilter that = (MovieFilter) o;
    return Objects.equals(genre, that.genre);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genre);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String genre;

    public MovieFilter build() {
      MovieFilter result = new MovieFilter();
      result.genre = this.genre;
      return result;
    }

    public Builder genre(String genre) {
      this.genre = genre;
      return this;
    }
  }
}
