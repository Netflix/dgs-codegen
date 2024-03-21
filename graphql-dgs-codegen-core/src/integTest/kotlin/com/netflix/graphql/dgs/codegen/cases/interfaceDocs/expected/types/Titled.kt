package com.netflix.graphql.dgs.codegen.cases.interfaceDocs.expected.types

import com.fasterxml.jackson.`annotation`.JsonTypeInfo
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmName

/**
 * Anything with a title!
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "__typename"
)
public sealed interface Titled {
    @Suppress("INAPPLICABLE_JVM_NAME")
    @get:JvmName("getTitle")
    public val title: String?
}
