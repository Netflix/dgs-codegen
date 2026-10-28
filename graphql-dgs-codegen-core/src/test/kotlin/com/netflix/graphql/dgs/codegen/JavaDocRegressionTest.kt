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

import com.netflix.graphql.dgs.codegen.generators.java.escapeJavaDocComparisons
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.StringWriter
import java.nio.file.Files
import javax.tools.ToolProvider

class JavaDocRegressionTest {
    @Test
    fun comparisonEscapingPreservesCompleteMarkupAndDollarSigns() {
        assertThat("created_at:<now - created_at:<=2024<br/> <b>bold</b> ${'$'}value".escapeJavaDocComparisons())
            .isEqualTo("created_at:&lt;now - created_at:&lt;=2024<br/> <b>bold</b> ${'$'}value")
        assertThat(
            "{@code List<String>} {@literal a < b} &lt;now".escapeJavaDocComparisons(),
        ).isEqualTo("{@code List<String>} {@literal a < b} &lt;now")
    }

    @Test
    fun schemaDescriptionWithComparisonProducesValidJavaDoc() {
        val schema =
            """
            "created_at:<now - created_at:<=2024 <br/> <b>bold</b> {@code a < b}"
            type Movie implements Titled {
                "title:<now" title: String
            }
            "interface:<now" interface Titled { "field:<=2024" title: String }
            "input:<now" input MovieFilter { "filter:<=2024" title: String }
            "enum:<now" enum Color { "red:<=2024" RED }
            type Query { "query:<now" search("argument:<=2024" filter: MovieFilter): Movie }
            """.trimIndent()
        val result =
            CodeGen(
                CodeGenConfig(schemas = setOf(schema), packageName = "example", addGeneratedAnnotation = false, generateClientApi = true),
            ).generate()
        val sources = Files.createTempDirectory("javadoc-sources")
        val docs = Files.createTempDirectory("javadoc-output")
        val generated =
            result.javaDataTypes + result.javaInterfaces + result.javaEnumTypes + result.javaQueryTypes + result.clientProjections
        val files =
            generated.map { javaFile ->
                javaFile.writeTo(sources)
                sources.resolve(javaFile.packageName().replace('.', '/')).resolve("${javaFile.typeSpec().name()}.java").toFile()
            }
        val diagnostics = StringWriter()
        val tool = ToolProvider.getSystemDocumentationTool()
        tool.getStandardFileManager(null, null, null).use { manager ->
            val task =
                tool.getTask(
                    diagnostics,
                    manager,
                    null,
                    null,
                    listOf("-quiet", "-Xdoclint:all,-missing", "-d", docs.toString(), "-classpath", classpath()),
                    manager.getJavaFileObjectsFromFiles(files),
                )
            try {
                assertThat(task.call()).withFailMessage(diagnostics.toString()).isTrue()
            } finally {
                sources.toFile().deleteRecursively()
                docs.toFile().deleteRecursively()
            }
        }
    }
}
