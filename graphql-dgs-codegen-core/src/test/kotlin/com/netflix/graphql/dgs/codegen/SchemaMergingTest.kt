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
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class SchemaMergingTest {
    private fun fooFields(config: CodeGenConfig): List<String> =
        CodeGen(config)
            .generate()
            .javaDataTypes
            .single { it.typeSpec().name() == "Foo" }
            .typeSpec()
            .fieldSpecs()
            .map { it.name() }

    private fun createJar(
        file: File,
        entries: List<Pair<String, String>>,
    ): File {
        file.parentFile.mkdirs()
        JarOutputStream(file.outputStream()).use { jar ->
            entries.forEach { (name, content) ->
                jar.putNextEntry(ZipEntry(name))
                jar.write(content.toByteArray())
                jar.closeEntry()
            }
        }
        return file
    }

    @Test
    fun `schema files are ordered by absolute path across roots`(
        @TempDir tempDir: Path,
    ) {
        val baseRoot = Files.createDirectories(tempDir.resolve("base")).toFile()
        val zRoot = Files.createDirectories(tempDir.resolve("z-root")).toFile()
        val aRoot = Files.createDirectories(tempDir.resolve("a-root")).toFile()
        File(baseRoot, "base.graphqls").writeText("type Query { foo: Foo } type Foo { base: String }")
        File(zRoot, "same.graphqls").writeText("extend type Foo { fromZ: String }")
        File(aRoot, "same.graphqls").writeText("extend type Foo { fromA: String }")

        // Every permutation of the configured roots yields the 8.7.0 order of the extensions: a-root, then z-root.
        listOf(baseRoot, zRoot, aRoot).let { roots ->
            listOf(roots, roots.reversed(), roots.drop(1) + roots.first()).forEach {
                Assertions
                    .assertThat(fooFields(CodeGenConfig(schemaFiles = it.toSet(), packageName = "com.example")))
                    .containsExactly("base", "fromA", "fromZ")
            }
        }
    }

    @Test
    fun `dependency jar schemas retain zip entry order`(
        @TempDir tempDir: Path,
    ) {
        val schemaJar =
            createJar(
                tempDir.resolve("schemas.jar").toFile(),
                listOf(
                    "c-extension.graphqls" to "extend type Foo { third: String }",
                    "a-base.graphqls" to "type Query { foo: Foo } type Foo { first: String }",
                    "b-extension.graphqls" to "extend type Foo { second: String }",
                ),
            )
        val reorderedJar =
            createJar(
                tempDir.resolve("reordered/schemas.jar").toFile(),
                listOf(
                    "a-base.graphqls" to "type Query { foo: Foo } type Foo { first: String }",
                    "b-extension.graphqls" to "extend type Foo { second: String }",
                    "c-extension.graphqls" to "extend type Foo { third: String }",
                ),
            )

        listOf(
            schemaJar to listOf("first", "third", "second"),
            reorderedJar to listOf("first", "second", "third"),
        ).forEach { (jar, expectedFields) ->
            val fooType =
                CodeGen(CodeGenConfig(schemaJarFilesFromDependencies = listOf(jar), packageName = "com.example"))
                    .generate()
                    .javaDataTypes
                    .single { type -> type.typeSpec().name() == "Foo" }
                    .typeSpec()
            Assertions
                .assertThat(fooType.fieldSpecs().map { field -> field.name() })
                .containsExactlyElementsOf(expectedFields)
            Assertions
                .assertThat(
                    fooType
                        .methodSpecs()
                        .single { constructor ->
                            constructor.isConstructor && constructor.parameters().isNotEmpty()
                        }.parameters()
                        .map { parameter ->
                            parameter.name()
                        },
                ).containsExactlyElementsOf(expectedFields)
        }
    }

    @Test
    fun `dependency jars are ordered by path, not by input order`(
        @TempDir tempDir: Path,
    ) {
        val aJar =
            createJar(
                tempDir.resolve("a/lib.jar").toFile(),
                listOf("schema.graphqls" to "type Query { foo: Foo } type Foo { base: String } extend type Foo { fromA: String }"),
            )
        val zJar = createJar(tempDir.resolve("z/lib.jar").toFile(), listOf("schema.graphqls" to "extend type Foo { fromZ: String }"))

        listOf(listOf(aJar, zJar), listOf(zJar, aJar)).forEach {
            Assertions
                .assertThat(fooFields(CodeGenConfig(schemaJarFilesFromDependencies = it, packageName = "com.example")))
                .containsExactly("base", "fromA", "fromZ")
        }
    }

    @Test
    fun `dependency type mapping precedence keeps the input jar order`(
        @TempDir tempDir: Path,
    ) {
        val schemaJar =
            createJar(
                tempDir.resolve("schema.jar").toFile(),
                listOf(
                    "schema.graphqls" to "scalar BigDecimal type Query { foo: Foo } type Foo { value: BigDecimal }",
                    "META-INF/dgs.codegen.typemappings" to "BigDecimal=java.math.BigDecimal",
                ),
            )
        val mappingJar =
            createJar(
                tempDir.resolve("mapping.jar").toFile(),
                listOf("META-INF/dgs.codegen.typemappings" to "BigDecimal=java.math.BigInteger"),
            )

        val result =
            CodeGen(
                CodeGenConfig(
                    schemaJarFilesFromDependencies = listOf(schemaJar, mappingJar),
                    packageName = "com.example",
                ),
            ).generate()

        Assertions
            .assertThat(
                result.javaDataTypes
                    .single { it.typeSpec().name() == "Foo" }
                    .typeSpec()
                    .fieldSpecs()
                    .single()
                    .type()
                    .toString(),
            ).isEqualTo("java.math.BigDecimal")
    }

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
