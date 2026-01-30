package com.netflix.graphql.dgs.codegen.java.testcases.projections.projectionWithUnion.expected.types;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(@JsonSubTypes.Type(value = Employee.class, name = "Employee"))
public interface U {
}
