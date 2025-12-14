package com.netflix.graphql.dgs.codegen.java.testcases.misc.typesWithUnderscoreField.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.String;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = MyInterfaceImpl.class, name = "MyInterfaceImpl"))
public interface MyInterface {
  String get_();

  void set_(String __);
}
