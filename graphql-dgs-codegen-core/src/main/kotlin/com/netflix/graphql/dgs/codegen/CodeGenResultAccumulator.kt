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

import com.netflix.graphql.dgs.codegen.generators.shared.DocFileSpec
import com.palantir.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import java.util.IdentityHashMap

/** Accumulates generated files without copying each growing result list on every merge. */
internal class CodeGenResultAccumulator {
    private var javaDataTypes: MutableList<JavaFile>? = null
    private var javaInterfaces: MutableList<JavaFile>? = null
    private var javaEnumTypes: MutableList<JavaFile>? = null
    private var javaDataFetchers: MutableList<JavaFile>? = null
    private var javaQueryTypes: MutableList<JavaFile>? = null
    private var clientProjections: MutableList<JavaFile>? = null
    private var javaConstants: MutableList<JavaFile>? = null
    private var kotlinDataTypes: MutableList<FileSpec>? = null
    private var kotlinInputTypes: MutableList<FileSpec>? = null
    private var kotlinInterfaces: MutableList<FileSpec>? = null
    private var kotlinEnumTypes: MutableList<FileSpec>? = null
    private var kotlinDataFetchers: MutableList<FileSpec>? = null
    private var kotlinConstants: MutableList<FileSpec>? = null
    private var kotlinClientTypes: MutableList<FileSpec>? = null
    private var docFiles: MutableList<DocFileSpec>? = null
    private var hasResult = false
    private var projectionsAreDeduplicated = false
    private var projectionDeduplicator: ClientProjectionDeduplicator? = null

    fun add(result: CodeGenResult) {
        if (result === CodeGenResult.EMPTY) {
            return
        }

        // EMPTY.merge(first) returns the first chunk unchanged, so its duplicates survive until a real merge.
        if (hasResult && !projectionsAreDeduplicated) {
            deduplicateExistingProjections()
        }
        hasResult = true

        javaDataTypes = append(javaDataTypes, result.javaDataTypes)
        javaInterfaces = append(javaInterfaces, result.javaInterfaces)
        javaEnumTypes = append(javaEnumTypes, result.javaEnumTypes)
        javaDataFetchers = append(javaDataFetchers, result.javaDataFetchers)
        javaQueryTypes = append(javaQueryTypes, result.javaQueryTypes)
        if (projectionsAreDeduplicated) {
            result.clientProjections.forEach(::addDistinctProjection)
        } else {
            clientProjections = append(clientProjections, result.clientProjections)
        }
        javaConstants = append(javaConstants, result.javaConstants)
        kotlinDataTypes = append(kotlinDataTypes, result.kotlinDataTypes)
        kotlinInputTypes = append(kotlinInputTypes, result.kotlinInputTypes)
        kotlinInterfaces = append(kotlinInterfaces, result.kotlinInterfaces)
        kotlinEnumTypes = append(kotlinEnumTypes, result.kotlinEnumTypes)
        kotlinDataFetchers = append(kotlinDataFetchers, result.kotlinDataFetchers)
        kotlinConstants = append(kotlinConstants, result.kotlinConstants)
        kotlinClientTypes = append(kotlinClientTypes, result.kotlinClientTypes)
        docFiles = append(docFiles, result.docFiles)
    }

    fun build(): CodeGenResult {
        if (!hasResult) {
            return CodeGenResult.EMPTY
        }

        return CodeGenResult(
            javaDataTypes = javaDataTypes.orEmpty().toList(),
            javaInterfaces = javaInterfaces.orEmpty().toList(),
            javaEnumTypes = javaEnumTypes.orEmpty().toList(),
            javaDataFetchers = javaDataFetchers.orEmpty().toList(),
            javaQueryTypes = javaQueryTypes.orEmpty().toList(),
            clientProjections = clientProjections.orEmpty().toList(),
            javaConstants = javaConstants.orEmpty().toList(),
            kotlinDataTypes = kotlinDataTypes.orEmpty().toList(),
            kotlinInputTypes = kotlinInputTypes.orEmpty().toList(),
            kotlinInterfaces = kotlinInterfaces.orEmpty().toList(),
            kotlinEnumTypes = kotlinEnumTypes.orEmpty().toList(),
            kotlinDataFetchers = kotlinDataFetchers.orEmpty().toList(),
            kotlinConstants = kotlinConstants.orEmpty().toList(),
            kotlinClientTypes = kotlinClientTypes.orEmpty().toList(),
            docFiles = docFiles.orEmpty().toList(),
        )
    }

    private fun deduplicateExistingProjections() {
        val existing = clientProjections
        clientProjections = null
        projectionsAreDeduplicated = true
        existing.orEmpty().forEach(::addDistinctProjection)
    }

    private fun addDistinctProjection(file: JavaFile) {
        val deduplicator = projectionDeduplicator ?: ClientProjectionDeduplicator().also { projectionDeduplicator = it }
        if (deduplicator.add(file)) {
            clientProjections = (clientProjections ?: mutableListOf()).also { it.add(file) }
        }
    }

    private fun <T> append(
        target: MutableList<T>?,
        values: List<T>,
    ): MutableList<T>? {
        if (values.isEmpty()) {
            return target
        }
        return (target ?: ArrayList(values.size)).also { it.addAll(values) }
    }
}

internal fun Sequence<CodeGenResult>.mergeCodeGenResults(initial: CodeGenResult = CodeGenResult.EMPTY): CodeGenResult =
    CodeGenResultAccumulator()
        .also { accumulator ->
            accumulator.add(initial)
            forEach(accumulator::add)
        }.build()

internal fun Iterable<CodeGenResult>.mergeCodeGenResults(): CodeGenResult = asSequence().mergeCodeGenResults()

internal fun mergeCodeGenResults(vararg results: CodeGenResult): CodeGenResult = results.asSequence().mergeCodeGenResults()

/** Retains merge's first-identical-wins behavior while checking each projection only once. */
internal class ClientProjectionDeduplicator {
    private val byName = HashMap<Pair<String, String>, MutableList<JavaFile>>()
    private val rendered = IdentityHashMap<JavaFile, String>()

    fun add(file: JavaFile): Boolean {
        val sameName = byName.getOrPut(file.packageName() to file.typeSpec().name()) { mutableListOf() }
        if (sameName.any { it === file || source(it) == source(file) }) {
            return false
        }
        sameName.add(file)
        return true
    }

    private fun source(file: JavaFile): String = rendered.getOrPut(file) { file.typeSpec().toString() }
}
