package com.netflix.graphql.dgs.codegen.java.testcases.dataclasses.dataClassWithDeclaredScalars.expected.types;

import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.Objects;

public class PageInfo {
  private String startCursor;

  private String endCursor;

  private boolean hasNextPage;

  private boolean hasPreviousPage;

  public PageInfo() {
  }

  public PageInfo(String startCursor, String endCursor, boolean hasNextPage,
      boolean hasPreviousPage) {
    this.startCursor = startCursor;
    this.endCursor = endCursor;
    this.hasNextPage = hasNextPage;
    this.hasPreviousPage = hasPreviousPage;
  }

  public String getStartCursor() {
    return startCursor;
  }

  public void setStartCursor(String startCursor) {
    this.startCursor = startCursor;
  }

  public String getEndCursor() {
    return endCursor;
  }

  public void setEndCursor(String endCursor) {
    this.endCursor = endCursor;
  }

  public boolean getHasNextPage() {
    return hasNextPage;
  }

  public void setHasNextPage(boolean hasNextPage) {
    this.hasNextPage = hasNextPage;
  }

  public boolean getHasPreviousPage() {
    return hasPreviousPage;
  }

  public void setHasPreviousPage(boolean hasPreviousPage) {
    this.hasPreviousPage = hasPreviousPage;
  }

  @Override
  public String toString() {
    return "PageInfo{startCursor='" + startCursor + "', endCursor='" + endCursor + "', hasNextPage='" + hasNextPage + "', hasPreviousPage='" + hasPreviousPage + "'}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PageInfo that = (PageInfo) o;
    return Objects.equals(startCursor, that.startCursor) &&
        Objects.equals(endCursor, that.endCursor) &&
        hasNextPage == that.hasNextPage &&
        hasPreviousPage == that.hasPreviousPage;
  }

  @Override
  public int hashCode() {
    return Objects.hash(startCursor, endCursor, hasNextPage, hasPreviousPage);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String startCursor;

    private String endCursor;

    private boolean hasNextPage;

    private boolean hasPreviousPage;

    public PageInfo build() {
      PageInfo result = new PageInfo();
      result.startCursor = this.startCursor;
      result.endCursor = this.endCursor;
      result.hasNextPage = this.hasNextPage;
      result.hasPreviousPage = this.hasPreviousPage;
      return result;
    }

    public Builder startCursor(String startCursor) {
      this.startCursor = startCursor;
      return this;
    }

    public Builder endCursor(String endCursor) {
      this.endCursor = endCursor;
      return this;
    }

    public Builder hasNextPage(boolean hasNextPage) {
      this.hasNextPage = hasNextPage;
      return this;
    }

    public Builder hasPreviousPage(boolean hasPreviousPage) {
      this.hasPreviousPage = hasPreviousPage;
      return this;
    }
  }
}
