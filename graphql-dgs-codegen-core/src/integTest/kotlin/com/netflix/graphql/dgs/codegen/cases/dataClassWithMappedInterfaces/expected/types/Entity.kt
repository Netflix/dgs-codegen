package com.netflix.graphql.dgs.codegen.cases.dataClassWithMappedInterfaces.expected.types

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import com.netflix.graphql.dgs.codegen.fixtures.Node
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = Product::class, name = "Product")
    ]
)
public sealed interface Entity : Node {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getId")
    override val id: String
}
