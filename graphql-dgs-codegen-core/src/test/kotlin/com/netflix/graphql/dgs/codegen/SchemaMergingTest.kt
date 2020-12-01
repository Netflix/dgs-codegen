package com.netflix.graphql.dgs.codegen

import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.nio.file.Paths

@ExperimentalStdlibApi
class SchemaMergingTest {
    @Test
    fun mergeSchemasJava() {

        val schemaDir = Paths.get("src/test/resources/schemas").toAbsolutePath().toFile()

        val codeGen = CodeGen(config = CodeGenConfig(schemaFiles = setOf(schemaDir), writeToFiles = false, generateClientApi = true))
        val result = codeGen.generate() as CodeGenResult

        Assertions.assertThat(result.dataTypes.size).isEqualTo(2)
        Assertions.assertThat(result.dataTypes.find { it.typeSpec.name == "Person" }!!.typeSpec.fieldSpecs).extracting("name").contains("name", "movies")

        val movieType = result.dataTypes.find { it.typeSpec.name == "Movie" }
        Assertions.assertThat(movieType).isNotNull
    }

    @Test
    fun mergeSchemasKotlin() {

        val schemaDir = Paths.get("src/test/resources/schemas").toAbsolutePath().toFile()

        val codeGen = CodeGen(config = CodeGenConfig(schemaFiles = setOf(schemaDir), writeToFiles = false, generateClientApi = true, language = Language.KOTLIN))
        val result = codeGen.generate() as KotlinCodeGenResult
        val type = result.dataTypes.find { it.name == "Person" }!!.members[0] as TypeSpec

        Assertions.assertThat(result.dataTypes.size).isEqualTo(2)
        Assertions.assertThat(type.propertySpecs).extracting("name").contains("name", "movies")

        val movieType = result.dataTypes.find { it.name == "Movie" }!!.members[0] as TypeSpec
        Assertions.assertThat(movieType).isNotNull
    }
}