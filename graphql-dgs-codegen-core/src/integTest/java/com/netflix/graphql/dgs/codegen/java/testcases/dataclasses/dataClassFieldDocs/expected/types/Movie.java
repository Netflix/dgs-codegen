package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassFieldDocs.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class Movie {
  /**
   * The original, non localized title with some specials characters : %!({[*$,.:;.
   */
  private String title;

  public Movie() {
  }

  public Movie(String title) {
    this.title = title;
  }

  /**
   * The original, non localized title with some specials characters : %!({[*$,.:;.
   */
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
    /**
     * The original, non localized title with some specials characters : %!({[*$,.:;.
     */
    private String title;

    public Movie build() {
      Movie result = new Movie();
      result.title = this.title;
      return result;
    }

    /**
     * The original, non localized title with some specials characters : %!({[*$,.:;.
     */
    public Builder title(String title) {
      this.title = title;
      return this;
    }
  }
}
