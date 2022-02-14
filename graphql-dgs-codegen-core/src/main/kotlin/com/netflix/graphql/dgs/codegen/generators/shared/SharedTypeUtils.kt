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

import graphql.language.ListType
import graphql.language.NonNullType
import graphql.language.Type
import graphql.language.TypeName

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
                comma = Comma(mappedTypeArg, startFrom),
            )
        }
    }
}

internal data class GenericSymbolsAhead(
    val openBracket: GenericSymbol.OpenBracket,
    val closeBracket: GenericSymbol.CloseBracket,
    val comma: GenericSymbol.Comma,
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

    if (!iterator.hasNext())
        return mappedType.toTypeName(false)

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

                if (stack.isEmpty())
                    return parameterized

                stack.last().second.add(parameterized)
            }
        }
    }

    if (stack.isNotEmpty()) throw IllegalArgumentException("Wrong mapped type $mappedType")
    return mappedType.toTypeName(true)
}

fun Type<*>.isID(): Boolean {
    return when (this) {
        is TypeName -> this.name == "ID"
        is NonNullType -> this.type.isID()
        is ListType -> false
        else -> false
    }
}