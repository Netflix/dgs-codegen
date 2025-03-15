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

package com.netflix.graphql.dgs.codegen.generators.kotlin2

import com.netflix.graphql.dgs.codegen.CodeGenConfig
import com.netflix.graphql.dgs.codegen.CodeGenResult
import com.netflix.graphql.dgs.codegen.generators.kotlin.KotlinDataFetcherGenerator
import com.squareup.kotlinpoet.FileSpec
import graphql.language.Document
import graphql.language.ObjectTypeDefinition

fun generateKotlin2DataFetcherInterfaces(config: CodeGenConfig, document: Document): List<FileSpec> =
    if (config.generateDataFetcherInterfaces) {
        document.definitions.asSequence()
            .filterIsInstance<ObjectTypeDefinition>()
            .filter { it.name == "Query" || it.name == "Mutation" || it.name == "Subscription" }
            .map { KotlinDataFetcherGenerator(config, document).generate(it) }
            .fold(CodeGenResult()) { result, next -> result.merge(next) }
            .kotlinDataFetchers
    } else {
        emptyList()
    }
