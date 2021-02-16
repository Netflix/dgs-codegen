package com.netflix.graphql.dgs.codegen.tests.generated.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;

public class Asset {
  private ReviewConnection reviews;

  public Asset() {
  }

  public Asset(ReviewConnection reviews) {
    this.reviews = reviews;
  }

  public ReviewConnection getReviews() {
    return reviews;
  }

  public void setReviews(ReviewConnection reviews) {
    this.reviews = reviews;
  }

  @Override
  public String toString() {
    return "Asset{" + "reviews='" + reviews + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset that = (Asset) o;
        return java.util.Objects.equals(reviews, that.reviews);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(reviews);
  }

  public static com.netflix.graphql.dgs.codegen.tests.generated.types.Asset.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private ReviewConnection reviews;

    public Asset build() {
      com.netflix.graphql.dgs.codegen.tests.generated.types.Asset result = new com.netflix.graphql.dgs.codegen.tests.generated.types.Asset();
          result.reviews = this.reviews;
          return result;
    }

    public com.netflix.graphql.dgs.codegen.tests.generated.types.Asset.Builder reviews(
        ReviewConnection reviews) {
      this.reviews = reviews;
      return this;
    }
  }
}
