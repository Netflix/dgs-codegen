package com.netflix.graphql.dgs.codegen.cases.skipCodegenOnInterfaceFields.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
public sealed interface Person {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getName")
    public val name: String?
}
