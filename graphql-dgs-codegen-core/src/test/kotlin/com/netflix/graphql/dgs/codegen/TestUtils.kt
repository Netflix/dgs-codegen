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
import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler.javac
import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.junit.platform.commons.util.ReflectionUtils
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path

fun assertCompilesJava(codeGenResult: CodeGenResult): Compilation {
    return assertCompilesJava(
        codeGenResult.clientProjections
            .plus(codeGenResult.javaQueryTypes)
            .plus(codeGenResult.javaEnumTypes)
            .plus(codeGenResult.javaDataTypes)
            .plus(codeGenResult.javaInterfaces)
    )
}

fun assertCompilesJava(javaFiles: Collection<JavaFile>): Compilation {
    val result = javac()
        .withOptions("-parameters")
        .compile(javaFiles.map(JavaFile::toJavaFileObject))
    CompilationSubject.assertThat(result).succeededWithoutWarnings()
    return result
}

fun assertCompilesKotlin(files: Collection<FileSpec>): Path {
    val srcDir = Files.createTempDirectory("src")
    val buildDir = Files.createTempDirectory("build")
    files.forEach { it.writeTo(srcDir) }

    K2JVMCompiler().run {
        val exitCode = execImpl(
            PrintingMessageCollector(
                System.out,
                MessageRenderer.WITHOUT_PATHS,
                false,
            ),
            Services.EMPTY,
            K2JVMCompilerArguments().apply {
                freeArgs = listOf(srcDir.toAbsolutePath().toString())
                destination = buildDir.toAbsolutePath().toString()
                classpath = System.getProperty("java.class.path")
                    .split(System.getProperty("path.separator"))
                    .filter {
                        File(it).exists() && File(it).canRead()
                    }.joinToString(":")
                noStdlib = true
                noReflect = true
            }
        )
        assertThat(exitCode).isEqualTo(ExitCode.OK)
    }

    return buildDir
}

fun codegenTestClassLoader(compilation: Compilation, parent: ClassLoader? = null): ClassLoader {
    return CodegenTestClassLoader(compilation, parent)
}

fun Compilation.toClassLoader(): ClassLoader {
    return codegenTestClassLoader(this, javaClass.classLoader)
}

@Suppress("UNCHECKED_CAST")
fun <T> invokeMethod(method: Method, target: Any, vararg args: Any): T {
    val result = ReflectionUtils.invokeMethod(method, target, *args)
    return result as T
}

const val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"
const val typesPackageName = "$basePackageName.types"
const val dataFetcherPackageName = "$basePackageName.datafetchers"
