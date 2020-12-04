package com.netflix.graphql.dgs.codegen

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler.javac
import com.squareup.javapoet.JavaFile

fun assertCompiles(javaFiles: Collection<JavaFile>) {
    val result = javac().compile(javaFiles.map(JavaFile::toJavaFileObject))
    CompilationSubject.assertThat(result).succeededWithoutWarnings()
}