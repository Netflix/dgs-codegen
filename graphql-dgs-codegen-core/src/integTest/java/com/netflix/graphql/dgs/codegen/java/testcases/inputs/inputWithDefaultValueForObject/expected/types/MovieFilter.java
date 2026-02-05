package com.netflix.graphql.dgs.codegen.java.testcases.inputs.inputWithDefaultValueForObject.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class MovieFilter {
  private Person director = new Person(){{setName("Damian");setCar(new Car(){{setBrand("Tesla");}});}};

  public MovieFilter() {
  }

  public MovieFilter(Person director) {
    this.director = director;
  }

  public Person getDirector() {
    return director;
  }

  public void setDirector(Person director) {
    this.director = director;
  }

  @Override
  public String toString() {
    return "MovieFilter{director='" + director + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MovieFilter that = (MovieFilter) o;
    return Objects.equals(director, that.director);
  }

  @Override
  public int hashCode() {
    return Objects.hash(director);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Person director = new Person(){{setName("Damian");setCar(new Car(){{setBrand("Tesla");}});}};

    public MovieFilter build() {
      MovieFilter result = new MovieFilter();
      result.director = this.director;
      return result;
    }

    public Builder director(Person director) {
      this.director = director;
      return this;
    }
  }
}
