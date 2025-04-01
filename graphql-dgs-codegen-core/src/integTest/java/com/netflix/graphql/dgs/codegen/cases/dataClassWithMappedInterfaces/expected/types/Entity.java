package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netflix.graphql.dgs.codegen.fixtures.JavaNode;
import java.lang.String;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Product.class, name = "Product"))
public interface Entity extends JavaNode {
  String getId();

  void setId(String id);
}
