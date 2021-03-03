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

class ClassnameShortener {
    companion object {
        /**
         * Takes a class name, and shortens it by taking each upper case letter, and the first following lower case letter.
         * Example: ThisIsATest becomes ThIsATe
         * This is required to prevent extremely long class/file names for projections.
         */
        fun shorten(name: String): String {
            val sb = StringBuilder()

            val split = name.split(Regex("(?=[A-Z])"))
            split.filter { it.isNotEmpty() }.forEach {
                sb.append(it.substring(0, if (it.length > 1) 2 else 1))
            }
            return sb.toString()
        }
    }
}