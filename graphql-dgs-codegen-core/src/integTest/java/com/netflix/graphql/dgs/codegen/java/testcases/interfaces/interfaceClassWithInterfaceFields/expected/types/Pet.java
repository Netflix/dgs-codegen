package com.netflix.graphql.dgs.codegen.java.testcases.interfaces.interfaceClassWithInterfaceFields.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.lang.String;
import java.util.List;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Dog.class, name = "Dog"),
    @JsonSubTypes.Type(value = Bird.class, name = "Bird")
})
public interface Pet {
  String getId();

  void setId(String id);

  String getName();

  void setName(String name);

  List<String> getAddress();

  void setAddress(List<String> address);

  Pet getMother();

  Pet getFather();
}
