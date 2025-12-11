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
import com.netflix.graphql.dgs.codegen.generators.shared.generatedAnnotationClassName
import com.palantir.javapoet.AnnotationSpec
import com.palantir.javapoet.ClassName
import com.palantir.javapoet.JavaFile
import com.palantir.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.Services
import org.junit.platform.commons.util.ReflectionUtils
import java.io.File
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import com.squareup.kotlinpoet.AnnotationSpec as KAnnotationSpec
import com.squareup.kotlinpoet.ClassName as KClassName
import com.squareup.kotlinpoet.TypeSpec as KTypeSpec

fun assertCompilesJava(codeGenResult: CodeGenResult): Compilation {
    val files =
        buildList {
            addAll(codeGenResult.clientProjections)
            addAll(codeGenResult.javaQueryTypes)
            addAll(codeGenResult.javaEnumTypes)
            addAll(codeGenResult.javaDataTypes)
            addAll(codeGenResult.javaInterfaces)
        }
    return assertCompilesJava(files)
}

fun assertCompilesJava(javaFiles: Collection<JavaFile>): Compilation {
    val result =
        javac()
            .withOptions("-parameters")
            .compile(javaFiles.map(JavaFile::toJavaFileObject))
    CompilationSubject.assertThat(result).succeededWithoutWarnings()
    return result
}

fun assertCompilesKotlin(
    codeGenResult: CodeGenResult,
    tests: Map<String, String> = emptyMap(),
) = assertCompilesKotlin(codeGenResult.kotlinSources(), tests)

fun assertCompilesKotlin(
    files: Collection<FileSpec>,
    tests: Map<String, String> = emptyMap(),
): Path {
    val srcDir = Files.createTempDirectory("src")
    val buildDir = Files.createTempDirectory("build")
    files.forEach { it.writeTo(srcDir) }
    tests.forEach { (file, content) ->
        val target = File("$srcDir/$file")
        target.toPath().parent.createDirectories()
        target.writeText(content)
    }

    K2JVMCompiler().run {
        val exitCode =
            exec(
                PrintingMessageCollector(
                    System.out,
                    MessageRenderer.WITHOUT_PATHS,
                    false,
                ),
                Services.EMPTY,
                K2JVMCompilerArguments().apply {
                    freeArgs = listOf(srcDir.toAbsolutePath().toString())
                    destination = buildDir.toAbsolutePath().toString()
                    classpath = classpath()
                    noStdlib = true
                    noReflect = true
                    jvmTarget = JvmTarget.JVM_17.description
                },
            )
        assertThat(exitCode).isEqualTo(ExitCode.OK)
    }

    return buildDir
}

fun classpath(): String =
    System
        .getProperty("java.class.path")
        .split(System.getProperty("path.separator"))
        .filter {
            File(it).exists() && File(it).canRead()
        }.joinToString(":")

fun codegenTestClassLoader(
    compilation: Compilation,
    parent: ClassLoader? = null,
): ClassLoader = CodegenTestClassLoader(compilation, parent)

fun Compilation.toClassLoader(): ClassLoader = codegenTestClassLoader(this, javaClass.classLoader)

@Suppress("UNCHECKED_CAST")
fun <T> invokeMethod(
    method: Method,
    target: Any,
    vararg args: Any,
): T {
    val result = ReflectionUtils.invokeMethod(method, target, *args)
    return result as T
}

fun List<FileSpec>.assertKotlinGeneratedAnnotation(shouldHaveDate: Boolean) =
    onEach {
        it.members
            .filterIsInstance(KTypeSpec::class.java)
            .forEach { typeSpec -> typeSpec.assertKotlinGeneratedAnnotation(it, shouldHaveDate) }
    }

fun List<JavaFile>.assertJavaGeneratedAnnotation(shouldHaveDate: Boolean) =
    onEach {
        it.typeSpec().assertJavaGeneratedAnnotation(shouldHaveDate)
    }

fun KTypeSpec.assertKotlinGeneratedAnnotation(
    fileSpec: FileSpec,
    shouldHaveDate: Boolean,
) {
    val generatedSpec =
        annotations
            .firstOrNull { it.canonicalName() == "$BASE_PACKAGE_NAME.Generated" }
    assertThat(generatedSpec)
        .`as`("@Generated annotation exists in %s at %s", this, fileSpec)
        .isNotNull

    val javaxGeneratedSpec =
        annotations.firstOrNull { it.canonicalName() == generatedAnnotationClassName }
    assertThat(javaxGeneratedSpec)
        .`as`("$generatedAnnotationClassName annotation exists in %s at %s", this, fileSpec)
        .isNotNull

    if (shouldHaveDate) {
        assertThat(javaxGeneratedSpec!!.members)
            .`as`("generatedAnnotationClassName has a date in %s at %s", this, fileSpec)
            .anyMatch { it.toString().startsWith("date = ") }
    } else {
        assertThat(javaxGeneratedSpec!!.members)
            .`as`("generatedAnnotationClassName has no date in %s at %s", this, fileSpec)
            .noneMatch { it.toString().startsWith("date = ") }
    }

    typeSpecs.forEach { it.assertKotlinGeneratedAnnotation(fileSpec, shouldHaveDate) }
}

fun TypeSpec.assertJavaGeneratedAnnotation(shouldHaveDate: Boolean) {
    val generatedSpec =
        annotations()
            .firstOrNull { it.canonicalName() == "$BASE_PACKAGE_NAME.Generated" }
    assertThat(generatedSpec)
        .`as`("@Generated annotation exists in %s", this)
        .isNotNull

    val jakartaGeneratedSpec =
        annotations().firstOrNull { it.canonicalName() == generatedAnnotationClassName }
    assertThat(jakartaGeneratedSpec)
        .`as`("$generatedAnnotationClassName annotation exists in %s", this)
        .isNotNull

    if (shouldHaveDate) {
        assertThat(jakartaGeneratedSpec!!.members().keys).contains("date")
    } else {
        assertThat(jakartaGeneratedSpec!!.members().keys).doesNotContain("date")
    }

    this.typeSpecs().forEach { it.assertJavaGeneratedAnnotation(shouldHaveDate) }
}

fun AnnotationSpec.canonicalName(): String = (type() as ClassName).canonicalName()

fun KAnnotationSpec.canonicalName() = (typeName as KClassName).canonicalName

const val BASE_PACKAGE_NAME = "com.netflix.graphql.dgs.codegen.tests.generated"
const val TYPES_PACKAGE_NAME = "$BASE_PACKAGE_NAME.types"
const val DATA_FETCHER_PACKAGE_NAME = "$BASE_PACKAGE_NAME.datafetchers"
