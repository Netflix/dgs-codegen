package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceWithUnderscoreFields.expected.Generated;
import java.lang.String;

@Generated
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = T.class, name = "T"))
public interface I {
  String get_id();

  void set_id(String _id);
}
