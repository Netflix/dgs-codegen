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

import java.util.*
import kotlin.collections.ArrayList

object CodeGeneratorUtils {

    enum class Case {
        LOWERCASE,
        UPPERCASE
    }

    /**
     * Transforms the input string, that should express a camel case notation, into one that expresses a snake case notation.
     *
     * @see StringUtils.splitByCharacterTypeCamelCase
     * */
    fun camelCasetoSnakeCase(input: String, case: Case = Case.LOWERCASE): String {
        val parts = splitByCharacterTypeCamelCase(input)
        return parts.joinToString(separator = "_") {
            when (case) {
                Case.LOWERCASE -> it.toLowerCase()
                Case.UPPERCASE -> it.toUpperCase()
            }
        }
    }

    /**
     * Mostly copied from Apache Commons StringUtils.splitByCharacterType
     */
    private fun splitByCharacterTypeCamelCase(str: String): Array<String> {

        if (str.isNullOrEmpty()) {
            return emptyArray()
        }
        val c = str.toCharArray()
        val list: MutableList<String> = ArrayList()
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
                    list.add(String(c, tokenStart, newTokenStart - tokenStart))
                    tokenStart = newTokenStart
                }
            } else {
                list.add(String(c, tokenStart, pos - tokenStart))
                tokenStart = pos
            }
            currentType = type
        }
        list.add(String(c, tokenStart, c.size - tokenStart))
        return list.toTypedArray()
    }
}
