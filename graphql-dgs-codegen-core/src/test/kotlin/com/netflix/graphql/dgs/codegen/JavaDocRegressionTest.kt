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
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import javax.tools.ToolProvider

class JavaDocRegressionTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun comparisonEscapingPreservesCompleteMarkupAndDollarSigns() {
        assertThat("created_at:<now - created_at:<=2024<br/> <b>bold</b> ${'$'}value".escapeJavaDocComparisons())
            .isEqualTo("created_at:&lt;now - created_at:&lt;=2024<br/> <b>bold</b> ${'$'}value")
        assertThat(
            "{@code List<String>} {@literal a < b} &lt;now".escapeJavaDocComparisons(),
        ).isEqualTo("{@code List<String>} {@literal a < b} &lt;now")
    }

    @ParameterizedTest
    @MethodSource("descriptionExamples")
    fun schemaDescriptionPreservesMarkupAndEscapesComparisonText(
        description: String,
        expected: String,
    ) {
        val schema = "\"\"\"$description\"\"\" type Example { value: String }"
        val result =
            CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = "example", addGeneratedAnnotation = false)).generate()
        val docs = generateJavaDoc(result)

        assertThat(
            result.javaDataTypes
                .single()
                .typeSpec()
                .javadoc()
                .toString(),
        ).isEqualTo(expected)
        assertThat(Files.readString(docs.resolve("example/types/Example.html"))).doesNotContain("&amp;lt;")
    }

    @Test
    fun incompleteMarkupDoesNotHideComparisons() {
        assertThat("{@code a < b".escapeJavaDocComparisons()).isEqualTo("{@code a &lt; b")
        assertThat("<!-- a < b".escapeJavaDocComparisons()).isEqualTo("&lt;!-- a &lt; b")
        assertThat("<img alt=\"a < b\"".escapeJavaDocComparisons()).isEqualTo("&lt;img alt=\"a &lt; b\"")
        assertThat("<b, c>d".escapeJavaDocComparisons()).isEqualTo("&lt;b, c>d")
        assertThat("<b:value>".escapeJavaDocComparisons()).isEqualTo("&lt;b:value>")
    }

    @Test
    fun commentTerminatorsInInlineLiteralsPreserveRenderedText() {
        val description = "{@code &lt; { a */ b } {@link Missing}} {@literal &amp; { c */ d }}"
        val schema = "\"\"\"$description\"\"\" type Example { value: String }"
        val result =
            CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = "example", addGeneratedAnnotation = false)).generate()
        val docs = generateJavaDoc(result)

        assertThat(Files.readString(docs.resolve("example/types/Example.html"))).contains(
            "<code>&amp;lt; &#123; a *&#47; b } &#123;&#64;link Missing}</code>",
            "&amp;amp; &#123; c *&#47; d }",
        )
    }

    @Test
    fun unicodeEscapesInInlineLiteralsPreserveRenderedText() {
        val description = "{@code C:\\users\\x &#92;} {@literal {x} \\u002a/} {@code \\\\u0041}"
        val schema = "\"\"\"$description\"\"\" type Example { value: String }"
        val result =
            CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = "example", addGeneratedAnnotation = false)).generate()
        val docs = generateJavaDoc(result)

        assertThat(Files.readString(docs.resolve("example/types/Example.html"))).contains(
            "<code>C:&#92;users\\x &amp;#92;</code>",
            "&#123;x} &#92;u002a/",
            "<code>\\\\u0041</code>",
        )
    }

    @Test
    fun deprecationDocumentationEscapesComparisonsAtEveryJavaEmissionSite() {
        val schema =
            """
            input Filter @deprecated(reason: "input:<now") {
                value: String @deprecated(reason: "field:<now & before */ after C:\\users\\x")
            }
            enum Color { RED @deprecated(reason: "enum:<now") }
            type Movie { color: Color }
            type Query {
                search(filter: Filter @deprecated(reason: "argument:<now")): Movie @deprecated(reason: "query:<now")
            }
            """.trimIndent()
        val result =
            CodeGen(
                CodeGenConfig(
                    schemas = setOf(schema),
                    packageName = "example",
                    addGeneratedAnnotation = false,
                    generateClientApi = true,
                    addDeprecatedAnnotation = true,
                ),
            ).generate()

        generateJavaDoc(result)
        val filter = result.javaDataTypes.single { it.typeSpec().name() == "Filter" }.toString()
        val color = result.javaEnumTypes.single().toString()
        val query = result.javaQueryTypes.single().toString()
        assertThat(filter).contains("input:&lt;now", "field:&lt;now &amp; before *&#47; after C:&#92;users\\x", "@Deprecated")
        assertThat(color).contains("enum:&lt;now", "@Deprecated")
        assertThat(query).contains("@deprecated query:&lt;now", "@deprecated argument:&lt;now", "@Deprecated")
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
        generateJavaDoc(result)
    }

    private fun generateJavaDoc(result: CodeGenResult): Path {
        assertCompilesJava(result)
        val sources = directory.resolve("sources")
        val docs = directory.resolve("docs")
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
                    listOf("-quiet", "-private", "-Xdoclint:all,-missing", "-d", docs.toString(), "-classpath", classpath()),
                    manager.getJavaFileObjectsFromFiles(files),
                )
            assertThat(task.call()).withFailMessage(diagnostics.toString()).isTrue()
        }
        return docs
    }

    companion object {
        @JvmStatic
        fun descriptionExamples(): List<Arguments> =
            listOf(
                "created_at:<now - created_at:<=2024" to "created_at:&lt;now - created_at:&lt;=2024",
                "created_at:<now and created_at:>2024" to "created_at:&lt;now and created_at:>2024",
                "x <y then z > 3" to "x &lt;y then z > 3",
                "a<b, c>d" to "a&lt;b, c>d",
                "List<String> of items" to "List&lt;String> of items",
                "{@literal {x} followed by a < b}" to "{@literal {x} followed by a < b}",
                "{@code if (ready) { run(); } else if (a < b) { stop(); }}" to
                    "{@code if (ready) { run(); } else if (a < b) { stop(); }}",
                "Visible <!-- a < b --> end" to "Visible <!-- a < b --> end",
                "<img src=\"image.png\" alt=\"a < b\">" to "<img src=\"image.png\" alt=\"a < b\">",
                "<img src='image.png' alt='a > b and c < d'>" to "<img src='image.png' alt='a > b and c < d'>",
                "<br/> <b>bold</b> <i>italic</i> <code>code</code>" to "<br/> <b>bold</b> <i>italic</i> <code>code</code>",
                "<p>text</p><ul><li>item</li></ul>" to "<p>text</p><ul><li>item</li></ul>",
                "<PRE>example</PRE> <a href=https://example.com>link</a>" to
                    "<PRE>example</PRE> <a href=https://example.com>link</a>",
                "&lt;now &gt;then &amp; ${'$'}value size:>5" to "&lt;now &gt;then &amp; ${'$'}value size:>5",
                "a & b && c" to "a &amp; b &amp;&amp; c",
                "&amp; &lt &! &#; &#x;" to "&amp; &amp;lt &amp;! &amp;#; &amp;#x;",
                "&lt; &gt; &amp; &quot; &apos; &copy; &#60; &#x3c;" to "&lt; &gt; &amp; &quot; &apos; &copy; &#60; &#x3c;",
                "{@code a && b} {@literal a & b}" to "{@code a && b} {@literal a & b}",
                "<a href='https://example.com?a=1&b=2'>link</a>" to "<a href='https://example.com?a=1&b=2'>link</a>",
                "<img src='image.png' alt='a & b'>" to "<img src='image.png' alt='a & b'>",
                "Visible <!-- a & b --> end" to "Visible <!-- a & b --> end",
                "a */ b" to "a *&#47; b",
                "{@code a */ b & c < d}" to "<code>a *&#47; b &amp; c &lt; d</code>",
                "{@literal {x} */ b < d}" to "&#123;x} *&#47; b &lt; d",
                "<img src='image.png' alt='a */ b'>" to "<img src='image.png' alt='a *&#47; b'>",
                "Visible <!-- a */ b --> end" to "Visible <!-- a *&#47; b --> end",
                "see C:\\users\\x" to "see C:&#92;users\\x",
                "end \\u002a/ x" to "end &#92;u002a/ x",
                "end *\\u002f x" to "end *&#92;u002f x",
                "keep \\uuuu0041 \\u005cu0042" to "keep &#92;uuuu0041 &#92;u005cu0042",
                "line \\u000a break" to "line &#92;u000a break",
                "\\\\u0041" to "\\\\u0041",
                "\\\\\\u0041" to "\\\\&#92;u0041",
                "\\\\\\\\u0041" to "\\\\\\\\u0041",
                "\\x \\U0041" to "\\x \\U0041",
                "{@code C:\\users\\x}" to "<code>C:&#92;users\\x</code>",
                "{@literal {x} \\u002a/}" to "&#123;x} &#92;u002a/",
                "{@code \\\\u0041}" to "{@code \\\\u0041}",
                "<img src='image.png' alt='C:\\users\\x'>" to "<img src='image.png' alt='C:&#92;users\\x'>",
                "Visible <!-- \\u002a/ --> end" to "Visible <!-- &#92;u002a/ --> end",
                "{@code a < b} then c:<d and e > f" to "{@code a < b} then c:&lt;d and e > f",
            ).map { (description, expected) -> Arguments.of(description, expected) }
    }
}
