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

import com.squareup.kotlinpoet.*
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import javax.annotation.processing.Filer
import javax.tools.StandardLocation

class DocFileSpec private constructor(
    builder: DocFileSpec.Builder
) {
    private val extension = "md"
    private val packageName: String = builder.packageName
    private val markdownText: String = builder.markdownText

    @Throws(IOException::class)
    public fun writeTo(out: Appendable) {
        out.append(markdownText)
    }

    /** Writes this to `directory` as UTF-8 using the standard directory structure.  */
    @Throws(IOException::class)
    public fun writeTo(directory: Path) {
        require(Files.notExists(directory) || Files.isDirectory(directory)) {
            "path $directory exists but is not a directory."
        }

        Files.createDirectories(directory)

        val outputPath = directory.resolve("$packageName.$extension")
        OutputStreamWriter(Files.newOutputStream(outputPath), StandardCharsets.UTF_8).use { writer -> writeTo(writer) }
    }

    /** Writes this to `directory` as UTF-8 using the standard directory structure.  */
    @Throws(IOException::class)
    public fun writeTo(directory: File): Unit = writeTo(directory.toPath())

    /** Writes this to `filer`.  */
    @Throws(IOException::class)
    public fun writeTo(filer: Filer) {
        val filerSourceFile = filer.createResource(
            StandardLocation.SOURCE_OUTPUT,
            packageName,
            "$packageName.$extension"
        )
        try {
            filerSourceFile.openWriter().use { writer -> writeTo(writer) }
        } catch (e: Exception) {
            try {
                filerSourceFile.delete()
            } catch (ignored: Exception) {
            }
            throw e
        }
    }

    public class Builder internal constructor(
        public val packageName: String,
        public var markdownText: String
    ) {
        public fun setMarkdownText(markdownText: String): DocFileSpec.Builder = apply {
            this.markdownText = markdownText
        }

        public fun build(): DocFileSpec {
            return DocFileSpec(this)
        }
    }
    public companion object {
        @JvmStatic public fun get(packageName: String, markdownText: String): DocFileSpec {
            return builder(packageName, markdownText).build()
        }

        @JvmStatic public fun builder(packageName: String, markdownText: String): DocFileSpec.Builder =
            DocFileSpec.Builder(packageName, markdownText)
    }
}
