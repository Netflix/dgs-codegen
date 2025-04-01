package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.datafetchers;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types.Product;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;

@DgsComponent
public class ProductsDatafetcher {
  @DgsData(
      parentType = "Query",
      field = "products"
  )
  public List<Product> getProducts(DataFetchingEnvironment dataFetchingEnvironment) {
    return null;
  }
}
