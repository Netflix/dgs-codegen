package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithExtendedType.expected.types;

import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MovieFilter {
  private String genre;

  private Integer releaseYear;

  public MovieFilter() {
  }

  public MovieFilter(String genre, Integer releaseYear) {
    this.genre = genre;
    this.releaseYear = releaseYear;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public Integer getReleaseYear() {
    return releaseYear;
  }

  public void setReleaseYear(Integer releaseYear) {
    this.releaseYear = releaseYear;
  }

  @Override
  public String toString() {
    return "MovieFilter{genre='" + genre + "', releaseYear='" + releaseYear + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MovieFilter that = (MovieFilter) o;
    return Objects.equals(genre, that.genre) &&
        Objects.equals(releaseYear, that.releaseYear);
  }

  @Override
  public int hashCode() {
    return Objects.hash(genre, releaseYear);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String genre;

    private Integer releaseYear;

    public MovieFilter build() {
      MovieFilter result = new MovieFilter();
      result.genre = this.genre;
      result.releaseYear = this.releaseYear;
      return result;
    }

    public Builder genre(String genre) {
      this.genre = genre;
      return this;
    }

    public Builder releaseYear(Integer releaseYear) {
      this.releaseYear = releaseYear;
      return this;
    }
  }
}
