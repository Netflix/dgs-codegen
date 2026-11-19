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
 */

package com.netflix.graphql.dgs.codegen

import com.netflix.graphql.dgs.codegen.generators.shared.DocFileSpec
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Test
import java.time.Duration
import javax.lang.model.element.Modifier

class CodeGenResultTest {
    // Same-named projections that differ must survive the merge: 8.7.0 wrote both and the last one won on disk
    @Test
    fun `client projections drop only identical duplicates`() {
        val original = projection("example.client", "MovieProjection", methodCount = 1)
        val identical = projection("example.client", "MovieProjection", methodCount = 1)
        val differentContent = projection("example.client", "MovieProjection", methodCount = 2)
        val sameNameInAnotherPackage = projection("example.admin", "MovieProjection", methodCount = 1)

        val merged =
            CodeGenResult(clientProjections = listOf(original))
                .merge(CodeGenResult(clientProjections = listOf(identical, differentContent, sameNameInAnotherPackage)))

        assertThat(merged.clientProjections).containsExactly(original, differentContent, sameNameInAnotherPackage)
        assertThat(merged.javaSources()).containsExactly(differentContent, sameNameInAnotherPackage)
    }

    @Test
    fun `generation accumulator matches merge order across projection chunks`() {
        val original = projection("example.client", "MovieProjection", methodCount = 1)
        val identical = projection("example.client", "MovieProjection", methodCount = 1)
        val differentContent = projection("example.client", "MovieProjection", methodCount = 2)
        val sameNameInAnotherPackage = projection("example.admin", "MovieProjection", methodCount = 1)
        val chunks =
            listOf(
                CodeGenResult(clientProjections = listOf(original, identical)),
                CodeGenResult.EMPTY,
                CodeGenResult(clientProjections = listOf(differentContent, original, sameNameInAnotherPackage)),
                CodeGenResult(clientProjections = listOf(identical)),
            )

        val expected = chunks.fold(CodeGenResult.EMPTY) { result, next -> result.merge(next) }
        val actual = chunks.asSequence().mergeCodeGenResults()

        assertThat(actual.clientProjections).containsExactlyElementsOf(expected.clientProjections)
        assertThat(actual.clientProjections).containsExactly(original, differentContent, sameNameInAnotherPackage)
        assertThat(actual.javaSources()).containsExactly(differentContent, sameNameInAnotherPackage)
    }

    @Test
    fun `generation accumulator preserves first chunk duplicates until a real merge`() {
        val original = projection("example.client", "MovieProjection", methodCount = 1)
        val identical = projection("example.client", "MovieProjection", methodCount = 1)
        val first = CodeGenResult(clientProjections = listOf(original, identical))

        val onlyChunk = sequenceOf(first).mergeCodeGenResults()
        val emptyChunk = sequenceOf(first, CodeGenResult.EMPTY).mergeCodeGenResults()
        val ordinaryEmptyChunk = sequenceOf(first, CodeGenResult()).mergeCodeGenResults()

        assertThat(onlyChunk.clientProjections).containsExactly(original, identical)
        assertThat(emptyChunk.clientProjections).containsExactly(original, identical)
        assertThat(ordinaryEmptyChunk.clientProjections).containsExactly(original)
    }

    @Test
    fun `generation accumulator preserves list order and published snapshots`() {
        val firstJava = projection("example.client", "First", methodCount = 0)
        val secondJava = projection("example.client", "Second", methodCount = 0)
        val firstKotlin = FileSpec.builder("example.client", "First").build()
        val secondKotlin = FileSpec.builder("example.client", "Second").build()
        val firstDoc = DocFileSpec.get("First", "first")
        val secondDoc = DocFileSpec.get("Second", "second")
        val mutableJavaDataTypes = mutableListOf(firstJava)
        val mutableKotlinDataTypes = mutableListOf(firstKotlin)
        val first =
            CodeGenResult(
                javaDataTypes = mutableJavaDataTypes,
                javaInterfaces = listOf(firstJava),
                javaEnumTypes = listOf(firstJava),
                javaDataFetchers = listOf(firstJava),
                javaQueryTypes = listOf(firstJava),
                clientProjections = listOf(firstJava),
                javaConstants = listOf(firstJava),
                kotlinDataTypes = mutableKotlinDataTypes,
                kotlinInputTypes = listOf(firstKotlin),
                kotlinInterfaces = listOf(firstKotlin),
                kotlinEnumTypes = listOf(firstKotlin),
                kotlinDataFetchers = listOf(firstKotlin),
                kotlinConstants = listOf(firstKotlin),
                kotlinClientTypes = listOf(firstKotlin),
                docFiles = listOf(firstDoc),
            )
        val second =
            CodeGenResult(
                javaDataTypes = listOf(secondJava),
                javaInterfaces = listOf(secondJava),
                javaEnumTypes = listOf(secondJava),
                javaDataFetchers = listOf(secondJava),
                javaQueryTypes = listOf(secondJava),
                clientProjections = listOf(secondJava),
                javaConstants = listOf(secondJava),
                kotlinDataTypes = listOf(secondKotlin),
                kotlinInputTypes = listOf(secondKotlin),
                kotlinInterfaces = listOf(secondKotlin),
                kotlinEnumTypes = listOf(secondKotlin),
                kotlinDataFetchers = listOf(secondKotlin),
                kotlinConstants = listOf(secondKotlin),
                kotlinClientTypes = listOf(secondKotlin),
                docFiles = listOf(secondDoc),
            )
        val accumulator = CodeGenResultAccumulator()
        accumulator.add(first)
        val snapshot = accumulator.build()
        mutableJavaDataTypes.add(secondJava)
        mutableKotlinDataTypes.add(secondKotlin)
        accumulator.add(second)
        val result = accumulator.build()

        assertThat(snapshot.javaDataTypes).containsExactly(firstJava)
        assertThat(snapshot.kotlinDataTypes).containsExactly(firstKotlin)
        assertThat(result.javaDataTypes).containsExactly(firstJava, secondJava)
        assertThat(result.javaInterfaces).containsExactly(firstJava, secondJava)
        assertThat(result.javaEnumTypes).containsExactly(firstJava, secondJava)
        assertThat(result.javaDataFetchers).containsExactly(firstJava, secondJava)
        assertThat(result.javaQueryTypes).containsExactly(firstJava, secondJava)
        assertThat(result.clientProjections).containsExactly(firstJava, secondJava)
        assertThat(result.javaConstants).containsExactly(firstJava, secondJava)
        assertThat(result.kotlinDataTypes).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinInputTypes).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinInterfaces).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinEnumTypes).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinDataFetchers).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinConstants).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.kotlinClientTypes).containsExactly(firstKotlin, secondKotlin)
        assertThat(result.docFiles).containsExactly(firstDoc, secondDoc)
    }

    @Test
    fun `merging projections does not repeatedly render accumulated Java sources`() {
        val projections = (0 until 1_000).map { projection("example.client", "Projection$it", methodCount = 50) }

        assertTimeout(Duration.ofSeconds(5)) {
            val merged =
                projections.fold(CodeGenResult.EMPTY) { result, projection ->
                    result.merge(CodeGenResult(clientProjections = listOf(projection)))
                }

            assertThat(merged.clientProjections).hasSize(projections.size)
        }
    }

    private fun projection(
        packageName: String,
        typeName: String,
        methodCount: Int,
    ): JavaFile {
        val type = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC)
        repeat(methodCount) { index ->
            type.addMethod(
                MethodSpec
                    .methodBuilder("field$index")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String::class.java)
                    .addStatement("return \$S", "value")
                    .build(),
            )
        }
        return JavaFile.builder(packageName, type.build()).build()
    }
}
