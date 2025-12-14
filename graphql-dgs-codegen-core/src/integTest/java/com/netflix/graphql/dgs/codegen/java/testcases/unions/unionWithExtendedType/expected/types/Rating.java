package com.netflix.graphql.dgs.codegen.java.testcases.unions.unionWithExtendedType.expected.types;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NONE
)
public class Rating implements SearchResult {
  private Integer stars;

  public Rating() {
  }

  public Rating(Integer stars) {
    this.stars = stars;
  }

  public Integer getStars() {
    return stars;
  }

  public void setStars(Integer stars) {
    this.stars = stars;
  }

  @Override
  public String toString() {
    return "Rating{stars='" + stars + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Rating that = (Rating) o;
    return Objects.equals(stars, that.stars);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stars);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private Integer stars;

    public Rating build() {
      Rating result = new Rating();
      result.stars = this.stars;
      return result;
    }

    public Builder stars(Integer stars) {
      this.stars = stars;
      return this;
    }
  }
}
