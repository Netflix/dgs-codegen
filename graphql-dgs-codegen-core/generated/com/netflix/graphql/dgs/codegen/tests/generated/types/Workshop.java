package com.netflix.graphql.dgs.codegen.tests.generated.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;

public class Workshop {
  private ReviewConnection reviews;

  private Asset assets;

  public Workshop() {
  }

  public Workshop(ReviewConnection reviews, Asset assets) {
    this.reviews = reviews;
    this.assets = assets;
  }

  public ReviewConnection getReviews() {
    return reviews;
  }

  public void setReviews(ReviewConnection reviews) {
    this.reviews = reviews;
  }

  public Asset getAssets() {
    return assets;
  }

  public void setAssets(Asset assets) {
    this.assets = assets;
  }

  @Override
  public String toString() {
    return "Workshop{" + "reviews='" + reviews + "'," +"assets='" + assets + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Workshop that = (Workshop) o;
        return java.util.Objects.equals(reviews, that.reviews) &&
                            java.util.Objects.equals(assets, that.assets);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(reviews, assets);
  }

  public static com.netflix.graphql.dgs.codegen.tests.generated.types.Workshop.Builder newBuilder(
      ) {
    return new Builder();
  }

  public static class Builder {
    private ReviewConnection reviews;

    private Asset assets;

    public Workshop build() {
                  com.netflix.graphql.dgs.codegen.tests.generated.types.Workshop result = new com.netflix.graphql.dgs.codegen.tests.generated.types.Workshop();
                      result.reviews = this.reviews;
          result.assets = this.assets;
                      return result;
    }

    public com.netflix.graphql.dgs.codegen.tests.generated.types.Workshop.Builder reviews(
        ReviewConnection reviews) {
      this.reviews = reviews;
      return this;
    }

    public com.netflix.graphql.dgs.codegen.tests.generated.types.Workshop.Builder assets(
        Asset assets) {
      this.assets = assets;
      return this;
    }
  }
}
