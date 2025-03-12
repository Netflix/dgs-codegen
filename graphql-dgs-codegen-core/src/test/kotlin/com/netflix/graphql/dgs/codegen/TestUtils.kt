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
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.Services
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
    val files = buildList {
        addAll(codeGenResult.clientProjections)
        addAll(codeGenResult.javaQueryTypes)
        addAll(codeGenResult.javaEnumTypes)
        addAll(codeGenResult.javaDataTypes)
        addAll(codeGenResult.javaInterfaces)
    }
    return assertCompilesJava(files)
}

fun assertCompilesJava(javaFiles: Collection<JavaFile>): Compilation {
    val result = javac()
        .withOptions("-parameters")
        .compile(javaFiles.map(JavaFile::toJavaFileObject))
    CompilationSubject.assertThat(result).succeededWithoutWarnings()
    return result
}

fun assertCompilesKotlin(codeGenResult: CodeGenResult, tests: Map<String, String> = emptyMap()) =
    assertCompilesKotlin(codeGenResult.kotlinSources(), tests)

fun assertCompilesKotlin(files: Collection<FileSpec>, tests: Map<String, String> = emptyMap()): Path {
    val srcDir = Files.createTempDirectory("src")
    val buildDir = Files.createTempDirectory("build")
    files.forEach { it.writeTo(srcDir) }
    tests.forEach { (file, content) ->
        val target = File("$srcDir/$file")
        target.toPath().parent.createDirectories()
        target.writeText(content)
    }

    K2JVMCompiler().run {
        val exitCode = execImpl(
            PrintingMessageCollector(
                System.out,
                MessageRenderer.WITHOUT_PATHS,
                false
            ),
            Services.EMPTY,
            K2JVMCompilerArguments().apply {
                freeArgs = listOf(srcDir.toAbsolutePath().toString())
                destination = buildDir.toAbsolutePath().toString()
                classpath = classpath()
                noStdlib = true
                noReflect = true
                jvmTarget = JvmTarget.JVM_17.description
            }
        )
        assertThat(exitCode).isEqualTo(ExitCode.OK)
    }

    return buildDir
}

fun classpath(): String {
    return System.getProperty("java.class.path")
        .split(System.getProperty("path.separator"))
        .filter {
            File(it).exists() && File(it).canRead()
        }.joinToString(":")
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

fun List<FileSpec>.assertKotlinGeneratedAnnotation() = onEach {
    it.members
        .filterIsInstance(KTypeSpec::class.java)
        .forEach { typeSpec -> typeSpec.assertKotlinGeneratedAnnotation(it) }
}

fun List<JavaFile>.assertJavaGeneratedAnnotation(shouldHaveDate: Boolean) = onEach {
    it.typeSpec.assertJavaGeneratedAnnotation(shouldHaveDate)
}

fun KTypeSpec.assertKotlinGeneratedAnnotation(fileSpec: FileSpec) {
    val generatedSpec = annotations
        .firstOrNull { it.canonicalName() == "$basePackageName.Generated" }
    assertThat(generatedSpec)
        .`as`("@Generated annotation exists in %s at %s", this, fileSpec)
        .isNotNull

    val javaxGeneratedSpec =
        annotations.firstOrNull { it.canonicalName() == generatedAnnotationClassName }
    assertThat(javaxGeneratedSpec)
        .`as`("$generatedAnnotationClassName annotation exists in %s at %s", this, fileSpec)
        .isNotNull

    typeSpecs.forEach { it.assertKotlinGeneratedAnnotation(fileSpec) }
}

fun TypeSpec.assertJavaGeneratedAnnotation(shouldHaveDate: Boolean) {
    val generatedSpec = annotations
        .firstOrNull { it.canonicalName() == "$basePackageName.Generated" }
    assertThat(generatedSpec)
        .`as`("@Generated annotation exists in %s", this)
        .isNotNull

    val jakartaGeneratedSpec =
        annotations.firstOrNull { it.canonicalName() == generatedAnnotationClassName }
    assertThat(jakartaGeneratedSpec)
        .`as`("$generatedAnnotationClassName annotation exists in %s", this)
        .isNotNull

    if (shouldHaveDate) {
        assertThat(jakartaGeneratedSpec!!.members.keys).contains("date")
    } else {
        assertThat(jakartaGeneratedSpec!!.members.keys).doesNotContain("date")
    }

    this.typeSpecs.forEach { it.assertJavaGeneratedAnnotation(shouldHaveDate) }
}

fun AnnotationSpec.canonicalName(): String = (type as ClassName).canonicalName()
fun KAnnotationSpec.canonicalName() = (typeName as KClassName).canonicalName

@ParameterizedTest
@CsvSource(
    value = [
        "'$PERSON_TYPE_SCHEMA',,Person",
        "'$PERSON_TYPE_SCHEMA',{name}Dto,PersonDto",
        "'$PERSON_TYPE_SCHEMA',{name}{schemaType}Dto,PersonTypeDto",
        "'$PERSON_TYPE_SCHEMA',{name}GraphQL{schemaType},PersonGraphQLType",
        "'$PERSON_TYPE_SCHEMA',{name}{schemaType},PersonType",
        "'$PERSON_INPUT_SCHEMA',,Person",
        "'$PERSON_INPUT_SCHEMA',{name}Dto,PersonDto",
        "'$PERSON_INPUT_SCHEMA',{name}{schemaType}Dto,PersonInputDto",
        "'$PERSON_INPUT_SCHEMA',{name}GraphQL{schemaType},PersonGraphQLInput",
        "'$PERSON_INPUT_SCHEMA',{name}{schemaType},PersonInput",
        "'$PERSON_INTERFACE_SCHEMA',,Person",
        "'$PERSON_INTERFACE_SCHEMA',{name}Dto,PersonDto",
        "'$PERSON_INTERFACE_SCHEMA',{name}{schemaType}Dto,PersonInterfaceDto",
        "'$PERSON_INTERFACE_SCHEMA',{name}GraphQL{schemaType},PersonGraphQLInterface",
        "'$PERSON_INTERFACE_SCHEMA',{name}{schemaType},PersonInterface",
        "'$ENUM_SCHEMA',,Color",
        "'$ENUM_SCHEMA',{name}Dto,ColorDto",
        "'$ENUM_SCHEMA',{name}{schemaType}Dto,ColorEnumDto",
        "'$ENUM_SCHEMA',{name}GraphQL{schemaType},ColorGraphQLEnum",
        "'$ENUM_SCHEMA',{name}{schemaType},ColorEnum"
    ]
)
@Target(AnnotationTarget.FUNCTION)
annotation class TemplateClassNameTest

@Language("GraphQL")
const val PERSON_TYPE_SCHEMA: String = "type Person { firstname: String, lastname: String }"

@Language("GraphQL")
const val PERSON_INPUT_SCHEMA = "input Person { firstname: String, lastname: String }"

@Language("GraphQL")
const val PERSON_INTERFACE_SCHEMA = "interface Person { firstname: String, lastname: String }"

@Language("GraphQL")
const val ENUM_SCHEMA = "enum Color { RED, GREEN, BLUE }"

@Language("GraphQL")
const val SHOW_INTERFACE_WITH_IMPLEMENTATIONS_SCHEMA = """
    interface Show @key(fields: "showId") {
        showId: Int
        title: String
        artworkUrl: String
    }
    
    type Movie implements Show {
        showId: Int
        title: String
        artworkUrl: String
        director: String
    }
    
    type Series implements Show {
        showId: Int
        title: String
        artworkUrl: String
        episodes: Int
    }
"""
const val SHOW_INTERFACE_WITH_IMPLEMENTATIONS_NAME_TEMPLATE = "{name}{schemaType}"
const val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"
const val typesPackageName = "$basePackageName.types"
const val dataFetcherPackageName = "$basePackageName.datafetchers"
