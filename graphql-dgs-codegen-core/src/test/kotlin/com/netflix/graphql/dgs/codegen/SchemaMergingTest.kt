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

import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class SchemaMergingTest {
    @Test
    fun mergeSchemasJava() {
        val schemaDir = Paths.get("src/test/resources/schemas").toAbsolutePath().toFile()

        val codeGen =
            CodeGen(
                config =
                    CodeGenConfig(
                        schemaFiles = setOf(schemaDir),
                        writeToFiles = false,
                        generateClientApi = true,
                    ),
            )
        val result = codeGen.generate()

        Assertions.assertThat(result.javaDataTypes.size).isEqualTo(2)
        Assertions
            .assertThat(
                result.javaDataTypes
                    .single {
                        it.typeSpec().name() == "Person"
                    }.typeSpec()
                    .fieldSpecs(),
            ).extracting("name")
            .contains("name", "movies")

        val movieType = result.javaDataTypes.find { it.typeSpec().name() == "Movie" }
        Assertions.assertThat(movieType).isNotNull
    }

    @Test
    fun mergeSchemasKotlin() {
        val schemaDir = Paths.get("src/test/resources/schemas").toAbsolutePath().toFile()

        val codeGen =
            CodeGen(
                config =
                    CodeGenConfig(
                        schemaFiles = setOf(schemaDir),
                        writeToFiles = false,
                        language = Language.KOTLIN,
                        generateClientApi = true,
                    ),
            )
        val result = codeGen.generate()
        val type = result.kotlinDataTypes.single { it.name == "Person" }.members[0] as TypeSpec

        Assertions.assertThat(result.kotlinDataTypes.size).isEqualTo(2)
        Assertions.assertThat(type.propertySpecs).extracting("name").contains("name", "movies")

        val movieType = result.kotlinDataTypes.single { it.name == "Movie" }.members[0] as TypeSpec
        Assertions.assertThat(movieType).isNotNull
    }
}
