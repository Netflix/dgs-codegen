/*
 * Copyright 2026 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.graphql.dgs.codegen

import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.collectAllFieldDefinitions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findEnumExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInputExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findInterfaceExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findTypeExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.SchemaExtensionsUtils.findUnionExtensions
import com.netflix.graphql.dgs.codegen.generators.shared.findSchemaTypeMapping
import graphql.language.Document
import graphql.language.InterfaceTypeDefinition
import graphql.language.ObjectTypeDefinition
import graphql.language.TypeDefinition
import graphql.language.TypeName
import graphql.parser.Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class SchemaIndexTest {
    private val document =
        Parser.parse(
            """
            directive @javaType(name: String!) on SCALAR
            scalar Date @javaType(name: "java.time.LocalDate")

            interface Node { id: ID! }
            extend interface Node { label: String }
            interface Node { legacyId: String }

            type Product implements Node { id: ID!, sku: String }
            extend type Product { price: Float }
            type Product { legacySku: String }

            input Filter { term: String }
            extend input Filter { limit: Int }
            enum Color { RED }
            extend enum Color { BLUE }
            union Result = Product
            extend union Result = Product
            """.trimIndent(),
        )
    private val index = SchemaIndex(document)

    @Test
    fun `indexed type lookup preserves scan semantics`() {
        val lookups = listOf("Product", "Node", "Date", "String", "Missing")
        val modes =
            listOf(false, true).flatMap { includeBaseTypes ->
                listOf(false, true).flatMap { includeScalarTypes ->
                    listOf(false, true).map { excludeExtensions ->
                        Triple(includeBaseTypes, includeScalarTypes, excludeExtensions)
                    }
                }
            }

        for (name in lookups) {
            for ((includeBaseTypes, includeScalarTypes, excludeExtensions) in modes) {
                val type = TypeName(name)
                val scanned =
                    type.findTypeDefinition(
                        document,
                        excludeExtensions = excludeExtensions,
                        includeBaseTypes = includeBaseTypes,
                        includeScalarTypes = includeScalarTypes,
                    )
                val indexed =
                    type.findTypeDefinition(
                        index,
                        excludeExtensions = excludeExtensions,
                        includeBaseTypes = includeBaseTypes,
                        includeScalarTypes = includeScalarTypes,
                    )

                assertThat(indexed).isSameAs(scanned)
            }
        }
    }

    @Test
    fun `extensions and duplicate definitions preserve document order`() {
        assertThat(findTypeExtensions("Product", index)).containsExactlyElementsOf(findTypeExtensions("Product", document.definitions))
        assertThat(findInputExtensions("Filter", index)).containsExactlyElementsOf(findInputExtensions("Filter", document.definitions))
        assertThat(findEnumExtensions("Color", index)).containsExactlyElementsOf(findEnumExtensions("Color", document.definitions))
        assertThat(findInterfaceExtensions("Node", index)).containsExactlyElementsOf(findInterfaceExtensions("Node", document.definitions))
        assertThat(findUnionExtensions("Result", index)).containsExactlyElementsOf(findUnionExtensions("Result", document.definitions))

        val product = document.definitions.filterIsInstance<ObjectTypeDefinition>().first { it.name == "Product" }
        assertThat(collectAllFieldDefinitions(product, index).map { it.name })
            .containsExactlyElementsOf(collectAllFieldDefinitions(product, document.definitions).map { it.name })

        val node = document.definitions.filterIsInstance<InterfaceTypeDefinition>().first { it.name == "Node" }
        assertThat(collectAllFieldDefinitions(node, index).map { it.name })
            .containsExactlyElementsOf(collectAllFieldDefinitions(node, document.definitions).map { it.name })
    }

    @Test
    fun `scalar mappings and interface implementations are indexed`() {
        assertThat(index.schemaTypeMapping("Date")).isEqualTo("java.time.LocalDate")
        assertThat(index.schemaTypeMapping("Missing")).isNull()
        assertThat(index.implementations("Node").map { it.name }).containsExactly("Product")
        assertThat(index.implementations("Missing")).isEmpty()
    }

    @Test
    fun `definition names are indexed by type`() {
        assertThat(index.hasObjectType("Product")).isTrue()
        assertThat(index.hasInterfaceType("Node")).isTrue()
        assertThat(index.hasEnumType("Color")).isTrue()
        assertThat(index.hasUnionType("Result")).isTrue()

        assertThat(index.hasObjectType("Node")).isFalse()
        assertThat(index.hasInterfaceType("Color")).isFalse()
        assertThat(index.hasEnumType("Result")).isFalse()
        assertThat(index.hasUnionType("Product")).isFalse()
    }

    @Test
    fun `implementation is indexed once when an interface is repeated`() {
        val duplicateImplementsDocument =
            Parser.parse(
                """
                interface Node { id: ID! }
                type Product implements Node & Node { id: ID! }
                type Product implements Node { id: ID!, sku: String }
                """.trimIndent(),
            )

        assertThat(SchemaIndex(duplicateImplementsDocument).implementations("Node").map { it.name })
            .containsExactly("Product", "Product")
    }

    @Test
    fun `same-named scalar and object preserve scan semantics in either order`() {
        val schemas =
            listOf(
                "scalar Shared\ntype Shared { value: String }",
                "type Shared { value: String }\nscalar Shared",
            )

        for (schema in schemas) {
            val sameNameDocument = Parser.parse(schema)
            val sameNameIndex = SchemaIndex(sameNameDocument)
            for (includeBaseTypes in listOf(false, true)) {
                for (includeScalarTypes in listOf(false, true)) {
                    for (excludeExtensions in listOf(false, true)) {
                        val scanned =
                            TypeName("Shared").findTypeDefinition(
                                sameNameDocument,
                                includeBaseTypes = includeBaseTypes,
                                includeScalarTypes = includeScalarTypes,
                                excludeExtensions = excludeExtensions,
                            )
                        val indexed =
                            TypeName("Shared").findTypeDefinition(
                                sameNameIndex,
                                includeBaseTypes = includeBaseTypes,
                                includeScalarTypes = includeScalarTypes,
                                excludeExtensions = excludeExtensions,
                            )

                        assertThat(indexed).isSameAs(scanned)
                    }
                }
            }
        }
    }

    @Test
    fun `indexed lookups match scans across repository schemas`() {
        val root = repositoryRoot()
        val schemaPaths =
            Files.walk(root).use { paths ->
                paths
                    .filter { path ->
                        val fileName = path.fileName.toString()
                        Files.isRegularFile(path) &&
                            path.none { it.toString() == "build" || it.toString() == ".git" } &&
                            (fileName.endsWith(".graphql") || fileName.endsWith(".graphqls"))
                    }.sorted()
                    .toList()
            }

        assertThat(schemaPaths).isNotEmpty()
        for (schemaPath in schemaPaths) {
            assertIndexedLookupsMatchScans(Parser.parse(Files.readString(schemaPath)))
        }
    }

    private fun assertIndexedLookupsMatchScans(schemaDocument: Document) {
        val schemaIndex = SchemaIndex(schemaDocument)
        val names =
            schemaDocument.definitions
                .filterIsInstance<TypeDefinition<*>>()
                .map { it.name }
                .plus(listOf("String", "Int", "Float", "Boolean", "ID", "Missing"))
                .distinct()
        val modes =
            listOf(false, true).flatMap { includeBaseTypes ->
                listOf(false, true).flatMap { includeScalarTypes ->
                    listOf(false, true).map { excludeExtensions ->
                        Triple(includeBaseTypes, includeScalarTypes, excludeExtensions)
                    }
                }
            }

        for (name in names) {
            for ((includeBaseTypes, includeScalarTypes, excludeExtensions) in modes) {
                val type = TypeName(name)
                assertThat(
                    type.findTypeDefinition(
                        schemaIndex,
                        excludeExtensions = excludeExtensions,
                        includeBaseTypes = includeBaseTypes,
                        includeScalarTypes = includeScalarTypes,
                    ),
                ).isSameAs(
                    type.findTypeDefinition(
                        schemaDocument,
                        excludeExtensions = excludeExtensions,
                        includeBaseTypes = includeBaseTypes,
                        includeScalarTypes = includeScalarTypes,
                    ),
                )
            }

            assertThat(findSchemaTypeMapping(schemaIndex, name)).isEqualTo(findSchemaTypeMapping(schemaDocument, name))
            assertThat(
                findTypeExtensions(name, schemaIndex),
            ).containsExactlyElementsOf(findTypeExtensions(name, schemaDocument.definitions))
            assertThat(
                findInputExtensions(name, schemaIndex),
            ).containsExactlyElementsOf(findInputExtensions(name, schemaDocument.definitions))
            assertThat(
                findEnumExtensions(name, schemaIndex),
            ).containsExactlyElementsOf(findEnumExtensions(name, schemaDocument.definitions))
            assertThat(
                findInterfaceExtensions(name, schemaIndex),
            ).containsExactlyElementsOf(findInterfaceExtensions(name, schemaDocument.definitions))
            assertThat(
                findUnionExtensions(name, schemaIndex),
            ).containsExactlyElementsOf(findUnionExtensions(name, schemaDocument.definitions))
        }

        for (type in schemaDocument.definitions.filterIsInstance<TypeDefinition<*>>()) {
            assertThat(collectAllFieldDefinitions(type, schemaIndex))
                .containsExactlyElementsOf(collectAllFieldDefinitions(type, schemaDocument.definitions))
        }
    }

    private fun repositoryRoot(): Path =
        generateSequence(Path.of("").toAbsolutePath()) { it.parent }
            .first { Files.exists(it.resolve("settings.gradle")) || Files.exists(it.resolve("settings.gradle.kts")) }
}
