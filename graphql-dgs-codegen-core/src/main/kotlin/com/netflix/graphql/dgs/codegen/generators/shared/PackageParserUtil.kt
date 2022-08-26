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

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.generators.java.ParserConstants
import graphql.language.StringValue

class PackageParserUtil {

    companion object {
        /**
         * Retrieves the package value in the directive.
         * If not present uses the default package in the config for that particular type of annotation.
         * If neither of them are supplied the package name will be an empty String
         * Also parses the  simpleName/className from the name argument in the directive
         */
        fun getAnnotationPackage(config: CodeGenConfig, name: String, type: String? = null): Pair<String, String> {
            var packageName = name.substringBeforeLast(".", "")
            packageName =
                if (packageName.isEmpty() && type != null) config.includeImports.getOrDefault(type, "") else packageName
            return packageName to name.substringAfterLast(".")
        }

        /**
         * This function is used to get the enum package value  from the configuration
         * It is stored in the configuration as follows
         * mapOf(annotationName to mapOf(enumType to enumPackageName)
         * If no key is found in the configuration an empty string is returned
         */
        fun getEnumPackage(config: CodeGenConfig, annotationName: String, enumType: String): String {
            return config.includeEnumImports[annotationName]?.getOrDefault(
                enumType, ""
            ) ?: ""
        }
    }
}