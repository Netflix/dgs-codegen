package com.netflix.graphql.dgs.codegen

import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.ParameterizedTypeName
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import javax.tools.DiagnosticCollector
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.ToolProvider

class ClientApiGenTest {

    val basePackageName = "com.netflix.graphql.dgs.codegen.tests.generated"

    @ExperimentalStdlibApi
    @Test
    fun generateQueryType() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PeopleGraphQLQuery")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateMutationType() {

        val schema = """
            type Mutation {
                updateMovie(movieId: ID, title: String): Movie
            }
            
            type Movie {
                movieId: ID
                title: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateMutationWithInputType() {

        val schema = """
            type Mutation {
                updateMovie(movie: MovieDescription): Movie
            }
            
            input MovieDescription {
                movieId: ID
                title: String
                actors: [String]
            }
            
            type Movie {
                movieId: ID
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("UpdateMovieGraphQLQuery")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateProjectionRoot() {

        val schema = """
            type Query {
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(1)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
    }

    @ExperimentalStdlibApi
    @Test
    fun generateProjectionRootTestWithCycles() {

        val schema = """
            type Query @extends {
                persons: [Person]
            }

            type Person {
             name: String
             friends: [Person]
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("PersonsFriendsProjection")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateInterfaceProjectionsWithCycles() {
        val schema = """
            type Query {
                search(title: String): [Show]
            }
            
            interface Show {
                title: String
            }
            
            type Movie implements Show {
                title: String
                duration: Int
                details: Details
            }
            
            type Details {
                 show: Show
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(5)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchMovieDetailsProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchMovieDetailsShowProjection")
        assertThat(codeGenResult.clientProjections[4].typeSpec.name).isEqualTo("SearchMovieDetailsShowMovieProjection")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }


    @ExperimentalStdlibApi
    @Test
    fun generateSubProjectionsWithDifferentRootTypes() {

        val schema = """
            type Query @extends {
                persons: [Person]
                friends: [Person]
            }

            type Person {
             name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("FriendsProjectionRoot")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateSubProjectionsWithDifferentParentTypes() {

        val schema = """
            type Query @extends {
                persons: [Person]
                details(name: String): Details
            }

            type Person {
               details: Details
            }
            
            type Details {
                name: String
                age: Integer
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("PersonsProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("PersonsDetailsProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("DetailsProjectionRoot")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateSubProjectionTypes() {

        val schema = """
            type Query {
                movies: [Movie]
            }
            
            type Movie {
                title: String
                actors: [Actor]
            }
            
            type Actor {
                name: String
                age: Integer
            }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(2)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("MoviesProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("MoviesActorsProjection")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }


    @ExperimentalStdlibApi
    @Test
    fun generateArgumentsForSimpleTypes() {

        val schema = """
            type Query {
                personSearch(lastname: String): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }

        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult
        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("lastname")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateArgumentsForEnum() {

        val schema = """
            type Query {
                personSearch(index: SearchIndex): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            enum SearchIndex {
                TEST, PROD
            }
            
        """.trimIndent()


        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun generateArgumentsForObjectType() {

        val schema = """
            type Query {
                personSearch(index: SearchIndex): [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            type SearchIndex {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PersonSearchGraphQLQuery")
        assertThat(codeGenResult.queryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun skipCodegen() {

        val schema = """
            type Query {
                persons: [Person]
                personSearch(index: SearchIndex): [Person] @skipcodegen
            }
            
            type Person {
                firstname: String
                lastname: String
            }
            
            type SearchIndex {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("PersonsGraphQLQuery")
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun interfaceReturnTypes() {
        val schema = """
            type Query {
                search(title: String): [Show]
            }
            
            interface Show {
                title: String
            }
            
            type Movie implements Show {
                title: String
                duration: Int
            }
            
            type Series implements Show {
                title: String
                episodes: Int
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.queryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.queryTypes[0].typeSpec.name).isEqualTo("SearchGraphQLQuery")
        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs[1].name).isEqualTo("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchSeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs[1].name).isEqualTo("episodes")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @ExperimentalStdlibApi
    @Test
    fun interfaceFragment() {
        val schema = """
            type Query {
                search(title: String): [Show]
            }
            
            interface Show {
                title: String
            }
            
            type Movie implements Show {
                title: String
                duration: Int
            }
            
            type Series implements Show {
                title: String
                episodes: Int
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchSeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("duration")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @ExperimentalStdlibApi
    @Test
    fun interfaceFragmentOnSubType() {
        val schema = """
            type Query {
                search(title: String): [Result]
            }
            
            type Result {
                show: Show
            }
            
            interface Show {
                title: String
            }
            
            type Movie implements Show {
                title: String
                duration: Int
            }
            
            type Series implements Show {
                title: String
                episodes: Int
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchShowProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchShowMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchShowSeriesProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("episodes")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("duration")

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @ExperimentalStdlibApi
    @Test
    fun unionFragment() {
        val schema = """
            type Query {
                search: [Result]
            }
            
            union Result = Movie | Actor

            type Movie {
                title: String
            }

            type Actor {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchActorProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }

    @ExperimentalStdlibApi
    @Test
    fun unionFragmentOnSubType() {
        val schema = """
            type Query {
                search(title: String): [Result]
            }
            
            type Result {
                result: SearchResult
            }
            
            union SearchResult = Movie | Actor

            type Movie {
                title: String
            }

            type Actor {
                name: String
            }
            
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true)).generate() as CodeGenResult

        assertThat(codeGenResult.clientProjections.size).isEqualTo(4)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("SearchResultProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onMovie")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs).extracting("name").contains("onActor")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("SearchResultMovieProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").contains("title")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs).extracting("name").doesNotContain("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.name).isEqualTo("SearchResultActorProjection")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").contains("name")
        assertThat(codeGenResult.clientProjections[3].typeSpec.methodSpecs).extracting("name").doesNotContain("title")

        val superclass = codeGenResult.clientProjections[3].typeSpec.superclass as ParameterizedTypeName
        assertThat(superclass.typeArguments[1]).extracting("simpleName").containsExactly("SearchProjectionRoot")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.enumTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.interfaces))
    }


    @ExperimentalStdlibApi
    @Test
    fun testScalarsDontGenerateProjections() {
        val schema = """
          type Query {
              movieCountry: MovieCountry
          }
          
          type MovieCountry {
            country: String
            movieId: Long
          }
          scalar Long

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf(Pair("Long", "java.lang.Long")))).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun testExtendRootProjection() {
        val schema = """
          type Query {
              people: [Person]
          }
          
          type Person {
            name: String
          }
          
          extend type Person {
            email: String
          }

        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf(Pair("Long", "java.lang.Long")))).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(1)
        assertThat(projections[0].typeSpec.name).isEqualTo("PeopleProjectionRoot")
        assertThat(projections[0].typeSpec.methodSpecs.size).isEqualTo(2)
        assertThat(projections[0].typeSpec.methodSpecs).extracting("name").containsExactly("name", "email")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }

    @ExperimentalStdlibApi
    @Test
    fun testExtendSubProjection() {
        val schema = """
          type Query {
            search: [SearchResult]
          }
          
          type SearchResult {
            movie: Movie
          }
          
          type Movie {
            title: String
          }
          
          extend type Movie {
            director: String
          }
        """.trimIndent()

        val codeGenResult = CodeGen(CodeGenConfig(schemas = setOf(schema), packageName = basePackageName, generateClientApi = true, typeMapping = mapOf(Pair("Long", "java.lang.Long")))).generate() as CodeGenResult
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)
        assertThat(projections[1].typeSpec.name).isEqualTo("SearchMovieProjection")
        assertThat(projections[1].typeSpec.methodSpecs.size).isEqualTo(3)
        assertThat(projections[1].typeSpec.methodSpecs).extracting("name").contains("title", "director", "<init>")

        compileGeneratedSources(codeGenResult.clientProjections.plus(codeGenResult.queryTypes).plus(codeGenResult.dataTypes).plus(codeGenResult.enumTypes))
    }
}

fun compileGeneratedSources(dataTypes: List<JavaFile>) {
    val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnosticCollector = DiagnosticCollector<JavaFileObject>()
    val manager = compiler.getStandardFileManager(
            diagnosticCollector, null, null )

    val generatedFiles = dataTypes.map { it.toJavaFileObject() }

    Files.createDirectories(Paths.get("compiled-sources"))
    val compilationResult = compiler.getTask(null, manager, diagnosticCollector, listOf("-d", "compiled-sources"), null, generatedFiles).call()

    if (!compilationResult) {
        fail<Boolean>("Error compiling generated sources: ${diagnosticCollector.diagnostics}")
    }
}