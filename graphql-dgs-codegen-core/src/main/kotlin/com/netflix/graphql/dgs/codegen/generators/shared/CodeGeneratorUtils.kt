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

object CodeGeneratorUtils {
    enum class Case {
        LOWERCASE,
        UPPERCASE,
    }

    /**
     * Transforms the input string expressing camel-case notation into one that expresses a snake-case notation.
     * */
    fun camelCaseToSnakeCase(
        input: String,
        case: Case = Case.LOWERCASE,
    ): String {
        val parts = splitByCharacterTypeCamelCase(input)
        return parts.joinToString(separator = "_") {
            when (case) {
                Case.LOWERCASE -> it.lowercase()
                Case.UPPERCASE -> it.uppercase()
            }
        }
    }

    fun String.capitalized(): String = replaceFirstChar(Character::toTitleCase)

    /**
     * Mostly copied from Apache Commons StringUtils.splitByCharacterType
     */
    private fun splitByCharacterTypeCamelCase(str: String): Array<String> {
        if (str.isEmpty()) {
            return emptyArray()
        }
        val c = str.toCharArray()
        val list = mutableListOf<String>()
        var tokenStart = 0
        var currentType = Character.getType(c[tokenStart])
        for (pos in tokenStart + 1 until c.size) {
            val type = Character.getType(c[pos])
            if (type == currentType) {
                continue
            }
            if (type == Character.LOWERCASE_LETTER.toInt() && currentType == Character.UPPERCASE_LETTER.toInt()) {
                val newTokenStart = pos - 1
                if (newTokenStart != tokenStart) {
                    val part = String(c, tokenStart, newTokenStart - tokenStart)
                    if (part != "_") {
                        list.add(part)
                    }
                    tokenStart = newTokenStart
                }
            } else {
                val part = String(c, tokenStart, pos - tokenStart)
                if (part != "_") {
                    list.add(part)
                }
                tokenStart = pos
            }
            currentType = type
        }
        val part = String(c, tokenStart, c.size - tokenStart)
        if (part != "_") {
            list.add(part)
        }
        return list.toTypedArray()
    }
}
