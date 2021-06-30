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

import com.google.testing.compile.Compilation
import java.io.ByteArrayOutputStream
import java.util.*
import javax.tools.JavaFileObject

internal class CodegenTestClassLoader(private val compilation: Compilation, parent: ClassLoader?) : ClassLoader(parent) {

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String): Class<*>? {
        val packageNameAsUnixPath = name.replace(".", "/")
        val normalizedName = "/CLASS_OUTPUT/$packageNameAsUnixPath.class"

        return Optional.ofNullable(
            compilation
                .generatedFiles()
                .find { it.kind == JavaFileObject.Kind.CLASS && it.name == normalizedName }
        ).map { fileObject ->
            val input = fileObject.openInputStream()
            val buffer = ByteArrayOutputStream()
            var data: Int = input.read()
            while (data != -1) {
                buffer.write(data)
                data = input.read()
            }
            input.close()
            val classData: ByteArray = buffer.toByteArray()
            defineClass(name, classData, 0, classData.size)
        }.orElse(super.loadClass(name))
    }
}
