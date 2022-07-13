/*
 *
 *  Copyright 2020 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.netflix.graphql.dgs.codegen.generators.shared

import com.netflix.graphql.dgs.codegen.generators.kotlin2.logger
import graphql.language.Document
import graphql.language.ScalarTypeDefinition
import graphql.language.StringValue
import java.time.Instant

internal sealed class GenericSymbol(open val index: Int) {
    class OpenBracket(str: String, startFrom: Int = 0) : GenericSymbol(str.indexOf("<", startFrom))
    class CloseBracket(str: String, startFrom: Int = 0) : GenericSymbol(str.indexOf(">", startFrom))
    class Comma(str: String, startFrom: Int = 0) : GenericSymbol(str.indexOf(",", startFrom))

    fun notFound(): Boolean = index == -1

    companion object {
        fun findNext(mappedTypeArg: String, startFrom: Int = 0): GenericSymbolsAhead {
            return GenericSymbolsAhead(
                openBracket = OpenBracket(mappedTypeArg, startFrom),
                closeBracket = CloseBracket(mappedTypeArg, startFrom),
                comma = Comma(mappedTypeArg, startFrom)
            )
        }
    }
}

internal data class GenericSymbolsAhead(
    val openBracket: GenericSymbol.OpenBracket,
    val closeBracket: GenericSymbol.CloseBracket,
    val comma: GenericSymbol.Comma
) {
    fun nextSymbol(): GenericSymbol? {
        return listOf(openBracket, closeBracket, comma).filterNot { it.notFound() }.minByOrNull { it.index }
    }
}

internal class GenericSymbolsAheadIterator(private val mappedTypeArg: String) : Iterator<GenericSymbolsAhead> {
    private var lastSymbolIndex = -1
    private var currentSymbolIndex = -1

    override fun next(): GenericSymbolsAhead {
        lastSymbolIndex = currentSymbolIndex

        val next = GenericSymbol.findNext(mappedTypeArg, lastSymbolIndex + 1)

        next.nextSymbol()?.apply {
            currentSymbolIndex = this.index
        }

        return next
    }

    override fun hasNext(): Boolean {
        return GenericSymbol.findNext(mappedTypeArg, getLastSymbolIndex() + 1).nextSymbol() != null
    }

    fun getLastSymbolIndex(): Int = lastSymbolIndex
}

internal fun genericSymbolsAheadIterator(mappedType: String) = GenericSymbolsAheadIterator(mappedType)

internal fun <T> parseMappedType(
    mappedType: String,
    toTypeName: String.(isGenericParam: Boolean) -> T,
    parameterize: (current: Pair<T, MutableList<T>>) -> T,
    onCloseBracketCallBack: ((current: Pair<T, MutableList<T>>, typeString: String) -> Unit)
): T {
    val stack = mutableListOf<Pair<T, MutableList<T>>>()
    val iterator = genericSymbolsAheadIterator(mappedType)

    if (!iterator.hasNext()) {
        return mappedType.toTypeName(false)
    }

    for (genericSymbolsAhead in iterator) {
        when (val symbolAhead = genericSymbolsAhead.nextSymbol()) {
            is GenericSymbol.OpenBracket -> {
                val startIndex = iterator.getLastSymbolIndex() + 1
                val typeString = mappedType.substring(startIndex, symbolAhead.index)
                stack.add(Pair(typeString.toTypeName(true), mutableListOf()))
            }
            is GenericSymbol.Comma -> {
                val typeString = mappedType.substring(iterator.getLastSymbolIndex() + 1, symbolAhead.index)
                if (typeString.trim().isNotEmpty()) {
                    stack.last().second.add(typeString.toTypeName(true))
                }
            }
            is GenericSymbol.CloseBracket -> {
                if (stack.isEmpty()) throw IllegalArgumentException("Wrong mapped type $mappedType")

                val current = stack.removeLast()
                if (iterator.getLastSymbolIndex() + 1 <= symbolAhead.index) {
                    val typeString = mappedType.substring(iterator.getLastSymbolIndex() + 1, symbolAhead.index)
                    if (typeString.trim().isNotEmpty()) {
                        onCloseBracketCallBack.let { it(current, typeString) }
                    }
                }

                val parameterized = parameterize(current)

                if (stack.isEmpty()) {
                    return parameterized
                }

                stack.last().second.add(parameterized)
            }
            else ->
                logger.info("Symbol ahead [$symbolAhead] didn't match any of the expected variations.")
        }
    }

    if (stack.isNotEmpty()) throw IllegalArgumentException("Wrong mapped type $mappedType")
    return mappedType.toTypeName(true)
}

internal const val JAVA_TYPE_DIRECTIVE_NAME = "javaType"

/**
 * Find the type mapping to use for [typeName] if one exists in the [document].
 * That is, if a [ScalarTypeDefinition] exists with the same name as [typeName] and is annotated
 * with a @javaType directive, return the value from that directive; otherwise return null.
 */
internal fun findSchemaTypeMapping(document: Document, typeName: String): String? {
    for (definition in document.definitions) {
        val scalarTypeDefinition = definition as? ScalarTypeDefinition
            ?: continue
        if (scalarTypeDefinition.name != typeName) {
            continue
        }
        if (!scalarTypeDefinition.hasDirective(JAVA_TYPE_DIRECTIVE_NAME)) {
            continue
        }
        val directive = scalarTypeDefinition.getDirectives(JAVA_TYPE_DIRECTIVE_NAME).singleOrNull()
            ?: throw IllegalArgumentException("multiple @$JAVA_TYPE_DIRECTIVE_NAME directives are defined")
        val nameArgument = directive.getArgument("name")
            ?: throw IllegalArgumentException("@$JAVA_TYPE_DIRECTIVE_NAME directive must contain 'name' argument")
        return (nameArgument.value as StringValue).value
    }
    return null
}

internal val generatedAnnotationClassName: String? = runCatching {
    Class.forName("javax.annotation.processing.Generated").canonicalName
}.getOrElse {
    runCatching {
        Class.forName("javax.annotation.Generated").canonicalName
    }.getOrNull()
}
internal val generatedDate: String = Instant.now().toString()
