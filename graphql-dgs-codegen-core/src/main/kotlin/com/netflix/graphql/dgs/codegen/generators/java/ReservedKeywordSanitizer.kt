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

package com.netflix.graphql.dgs.codegen.generators.java

class ReservedKeywordSanitizer {

    companion object {
        private val reservedKeywords = setOf("import", "_", "root", "parent", "interface", "boolean", "enum")
        private const val prefix = "_"

        fun sanitize(originalName: String): String {
            return if (reservedKeywords.contains(originalName)) {
                "$prefix$originalName"
            } else {
                originalName
            }
        }
    }
}
