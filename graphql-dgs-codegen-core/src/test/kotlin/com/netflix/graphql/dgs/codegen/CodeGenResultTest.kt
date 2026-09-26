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

import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.MethodSpec
import com.palantir.javapoet.TypeSpec
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
