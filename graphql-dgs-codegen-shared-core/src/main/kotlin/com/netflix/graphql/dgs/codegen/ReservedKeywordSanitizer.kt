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

package com.netflix.graphql.dgs.codegen

import javax.lang.model.SourceVersion

abstract class ReservedKeywordSanitizer {
    protected abstract val reservedKeywords: Set<String>
    private val prefix: String = "_"

    fun sanitize(originalName: String): String =
        if (isKeyword(originalName)) {
            "$prefix$originalName"
        } else {
            originalName
        }

    fun desanitize(name: String): String {
        if (!name.startsWith(prefix) || name.length <= prefix.length) return name
        val desanitizedName = name.substring(prefix.length)
        return if (isKeyword(desanitizedName)) desanitizedName else name
    }

    private fun isKeyword(name: String): Boolean = name in reservedKeywords || SourceVersion.isKeyword(name)
}
