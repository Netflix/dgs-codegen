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

package com.netflix.graphql.dgs.codegen.clientapi

import com.netflix.graphql.dgs.codegen.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientApiGenQueryTest {
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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("PeopleGraphQLQuery")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun generateQueryTypeWithComments() {

        val schema = """
            type Query {
                ""${'"'}
                All the people
                ""${'"'}
                people: [Person]
            }
            
            type Person {
                firstname: String
                lastname: String
            }           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("PeopleGraphQLQuery")
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.javadoc.toString()).isEqualTo(
            """
            All the people
            """.trimIndent()
        )

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

    @Test
    fun generateQueryTypesWithTypeExtensions() {

        val schema = """
            extend type Person {
                preferences: Preferences
            }
            
            type Preferences {
                userId: ID!
            }
            
            type Query @extends {
                getPerson: Person
            }
        
            type Person {
                personId: ID!
                linkedIdentities: LinkedIdentities
            }
           
           type LinkedIdentities {
               employee: Employee
           }
           
           type Employee {
                id: ID!
                person: Person!
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("GetPersonGraphQLQuery")

        assertCompilesJava(codeGenResult)
    }

    @Test
    fun generateOnlyRequiredDataTypesForQuery() {
        val schema = """
            type Query {
                shows(showFilter: ShowFilter): [Video]
                people(personFilter: PersonFilter): [Person]
            }
            
            union Video = Show | Movie
            
            type Movie {
                title: String
                duration: Int
                related: Related
            }
            
            type Related {
                 video: Video
            }
            
            type Show {
                title: String
                tags(from: Int, to: Int, sourceType: SourceType): [ShowTag]
                isLive(countryFilter: CountryFilter): Boolean
            }
            
            enum ShouldNotInclude { YES, NO }
            
            input NotUsed {
                field: String
            }
            
            input ShowFilter {
                title: String
                showType: ShowType
                similarTo: SimilarityInput
            }
            
            input SimilarityInput {
                tags: [String]
            }
            
            enum ShowType {
                MOVIE, SERIES
            }
            
            input CountryFilter {
                countriesToExclude: [String]
            }
                 
            enum SourceType { FOO, BAR }
           
            type Person {
                name: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                includeQueries = setOf("shows"),
                generateDataTypes = false,
                writeToFiles = false
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowFilter", "SimilarityInput", "CountryFilter")
        assertThat(codeGenResult.javaEnumTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowType", "SourceType")
        assertThat(codeGenResult.javaQueryTypes)
            .extracting("typeSpec").extracting("name").containsExactly("ShowsGraphQLQuery")
        assertThat(codeGenResult.clientProjections)
            .extracting("typeSpec").extracting("name").containsExactly(
                "ShowsProjectionRoot",
                "Shows_ShowProjection",
                "Shows_MovieProjection",
                "Shows_Movie_RelatedProjection",
                "Shows_Movie_Related_VideoProjection",
                "Shows_Movie_Related_Video_ShowProjection",
                "Shows_Movie_Related_Video_MovieProjection"
            )

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaDataTypes + codeGenResult.javaEnumTypes)
    }

    @Test
    fun generateRecursiveInputTypes() {

        val schema = """
            type Query {
                movies(filter: MovieQuery): [String]
            }
            
            input MovieQuery {
                booleanQuery: BooleanQuery!
                titleFilter: String
            }
            
            input BooleanQuery {
                first: MovieQuery!
                second: MovieQuery!
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateDataTypes = false,
                generateClientApi = true,
                includeQueries = setOf("movies")
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes.size).isEqualTo(2)
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.name).isEqualTo("MovieQuery")
        assertThat(codeGenResult.javaDataTypes[1].typeSpec.name).isEqualTo("BooleanQuery")

        assertCompilesJava(codeGenResult.javaDataTypes)
    }

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("lastname")

        assertCompilesJava(codeGenResult.clientProjections + codeGenResult.javaQueryTypes)
    }

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes
        )
    }

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("PersonSearchGraphQLQuery")
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.typeSpecs[0].methodSpecs[1].name).isEqualTo("index")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes
        )
    }

    @Test
    fun includeQueryConfig() {

        val schema = """
            type Query {
                movieTitles: [String]
                actorNames: [String]
            }           
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                includeQueries = setOf("movieTitles"),
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("MovieTitlesGraphQLQuery")

        assertCompilesJava(codeGenResult)
    }

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("PersonsGraphQLQuery")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes
        )
    }

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

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("SearchGraphQLQuery")
        assertThat(codeGenResult.clientProjections.size).isEqualTo(3)
        assertThat(codeGenResult.clientProjections[0].typeSpec.name).isEqualTo("SearchProjectionRoot")
        assertThat(codeGenResult.clientProjections[0].typeSpec.methodSpecs[0].name).isEqualTo("title")
        assertThat(codeGenResult.clientProjections[1].typeSpec.name).isEqualTo("Search_MovieProjection")
        assertThat(codeGenResult.clientProjections[1].typeSpec.methodSpecs[2].name).isEqualTo("duration")
        assertThat(codeGenResult.clientProjections[2].typeSpec.name).isEqualTo("Search_SeriesProjection")
        assertThat(codeGenResult.clientProjections[2].typeSpec.methodSpecs[2].name).isEqualTo("episodes")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
    }

    @Test
    fun interfaceWithKeywords() {
        val schema = """
            type Query {
              queryRoot: QueryRoot
            }

            interface HasDefaultField {
              default: String
              public: String
              private: Boolean
            }
            
            type QueryRoot implements HasDefaultField {
                name: String
                default: String
                public: String
                private: Boolean
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaQueryTypes[0].typeSpec.name).isEqualTo("QueryRootGraphQLQuery")

        assertThat(codeGenResult.javaInterfaces.size).isEqualTo(1)
        assertThat(codeGenResult.javaInterfaces[0].typeSpec.name).isEqualTo("HasDefaultField")

        assertThat(codeGenResult.javaDataTypes.size).isEqualTo(1)
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs.size).isEqualTo(4)
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs[0].name).isEqualTo("name")
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs[1].name).isEqualTo("_default")
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs[2].name).isEqualTo("_public")

        assertCompilesJava(
            codeGenResult.clientProjections + codeGenResult.javaQueryTypes + codeGenResult.javaEnumTypes + codeGenResult.javaDataTypes + codeGenResult.javaInterfaces
        )
    }

    @Test
    fun `The Query API should support sub-projects on fields with Basic Types`() {
        // given
        val schema = """
            type Query {
                someField: Foo
            }
            
            type Foo {
                stringField(arg: Boolean): String
                stringArrayField(arg: Boolean): [String]
                intField(arg: Boolean): Int
                intArrayField(arg: Boolean): [Int]
                booleanField(arg: Boolean): Boolean
                booleanArrayField(arg: Boolean): [Boolean]
                floatField(arg: Boolean): Float
                floatArrayField(arg: Boolean): [Float]
            }
        """.trimIndent()
        // when
        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                writeToFiles = true
            )
        ).generate()
        // then
        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        // assert Type classes
        assertThat(testClassLoader.loadClass("$basePackageName.types.Foo")).isNotNull
        // assert root projection classes
        val rootProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeFieldProjectionRoot")
        assertThat(rootProjectionClass).isNotNull
        assertThat(rootProjectionClass).hasPublicMethods(
            "stringField",
            "stringArrayField",
            "intField",
            "intArrayField",
            "booleanField",
            "booleanArrayField",
            "floatField",
            "floatArrayField"
        )
        // fields projections
        val stringFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_StringFieldProjection")
        assertThat(rootProjectionClass).isNotNull
        // stringField
        assertThat(
            rootProjectionClass.getMethod("stringField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "stringField", java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(stringFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")
        // stringArrayField
        val stringArrayFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_StringArrayFieldProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(
            rootProjectionClass.getMethod("stringArrayField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "stringArrayField",
                java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(stringArrayFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")

        // booleanField
        val booleanFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_BooleanFieldProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(
            rootProjectionClass.getMethod("booleanField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "booleanField",
                java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(booleanFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")

        // booleanArrayField
        val booleanArrayFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_BooleanArrayFieldProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(
            rootProjectionClass.getMethod("booleanArrayField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "booleanArrayField", java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(booleanArrayFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")

        // floatField
        val floatFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_FloatFieldProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(
            rootProjectionClass.getMethod("floatField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "floatField", java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(floatFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")

        // booleanArrayField
        val floatArrayFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_FloatArrayFieldProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(
            rootProjectionClass.getMethod("floatArrayField")
        ).isNotNull
            .returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod(
                "floatArrayField", java.lang.Boolean::class.java
            )
        ).isNotNull
            .returns(floatArrayFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")
    }

    @Test
    fun `The Query API should support sub-projects on fields with Scalars`() {
        val schema = """
          type Query {
              someField: Foo
          }
          
          type Foo {
            ping(arg: Boolean): Long
          }
          
          scalar Long
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long"),
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)

        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        // assert Type classes
        assertThat(testClassLoader.loadClass("$basePackageName.types.Foo")).isNotNull
        // assert root projection classes
        val rootProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeFieldProjectionRoot")
        assertThat(rootProjectionClass).isNotNull
        assertThat(rootProjectionClass).hasPublicMethods("ping")
        // scalar field
        val scalarFieldProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeField_PingProjection")
        assertThat(rootProjectionClass).isNotNull

        assertThat(rootProjectionClass.getMethod("ping")).isNotNull.returns(rootProjectionClass) { it.returnType }

        assertThat(
            rootProjectionClass.getMethod("ping", java.lang.Boolean::class.java)
        ).isNotNull
            .returns(scalarFieldProjectionClass) { it.returnType }
            .extracting { m -> m.parameters.mapIndexed { index, parameter -> index to parameter.name } }
            .asList()
            .containsExactly(0 to "arg")
    }

    @Test
    fun `Should be able to generate a valid client when java keywords are used as field names`() {
        val schema = """
          type Query {
              someField: Foo
          }
          
          type Foo {
            ping(arg: Boolean): Long
            # ---
            parent: Boolean
            root: Boolean
            # --- 
            abstract: Boolean 
            assert: Boolean   
            boolean: Boolean  
            break: Boolean    
            byte: Boolean     
            case: Boolean     
            catch: Boolean    
            char: Boolean     
            # class: Boolean -- not supported
            const: Boolean    
            continue: Boolean     
            default: Boolean      
            do: Boolean           
            double: Boolean       
            else: Boolean         
            enum: Boolean         
            extends: Boolean      
            final: Boolean        
            finally: Boolean      
            float: Boolean        
            for: Boolean          
            goto: Boolean         
            if: Boolean           
            implements: Boolean   
            import: Boolean       
            instanceof: Boolean   
            int: Boolean          
            interface: Boolean    
            long: Boolean         
            native: Boolean      
            new: Boolean          
            package: Boolean      
            private: Boolean      
            protected: Boolean    
            public: Boolean       
            return: Boolean       
            short: Boolean        
            static: Boolean       
            strictfp: Boolean     
            super: Boolean        
            switch: Boolean
            synchronized: Boolean   
            this: Boolean           
            throw: Boolean          
            throws: Boolean         
            transient: Boolean      
            try: Boolean            
            void: Boolean           
            volatile: Boolean       
            while: Boolean          
          }
          
          scalar Long
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateDataTypes = true,
                generateClientApi = true,
                typeMapping = mapOf("Long" to "java.lang.Long"),
            )
        ).generate()
        val projections = codeGenResult.clientProjections
        assertThat(projections.size).isEqualTo(2)

        val testClassLoader = assertCompilesJava(codeGenResult).toClassLoader()
        // assert Type classes
        assertThat(testClassLoader.loadClass("$basePackageName.types.Foo")).isNotNull
        // assert root projection classes
        val rootProjectionClass =
            testClassLoader.loadClass("$basePackageName.client.SomeFieldProjectionRoot")
        assertThat(rootProjectionClass).isNotNull
        assertThat(rootProjectionClass).hasPublicMethods("ping")
        assertThat(rootProjectionClass).hasPublicMethods(
            "_parent",
            "_root",
            // ----
            "_abstract",
            "_assert",
            "_boolean",
            "_break",
            "_byte",
            "_case",
            "_catch",
            "_char",
            "_const",
            "_continue",
            "_default",
            "_do",
            "_double",
            "_else",
            "_enum",
            "_extends",
            "_final",
            "_finally",
            "_float",
            "_for",
            "_goto",
            "_if",
            "_implements",
            "_import",
            "_instanceof",
            "_int",
            "_interface",
            "_long",
            "_native",
            "_new",
            "_package",
            "_private",
            "_protected",
            "_public",
            "_return",
            "_short",
            "_static",
            "_strictfp",
            "_super",
            "_switch",
            "_synchronized",
            "_this",
            "_throw",
            "_throws",
            "_transient",
            "_try",
            "_void",
            "_volatile",
            "_while"
        )
    }

    @Test
    fun `Should be able to generate successfully when java keywords and default value are used as input types`() {
        val schema = """
            type Query {
                foo(fooInput: FooInput): Baz
                bar(barInput: BarInput): Baz
            }
            
            input FooInput {
                public: Boolean = true
            }
            
            input BarInput {
                public: Boolean
            }

            type Baz {
                public: Boolean
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateDataTypes = false,
                generateClientApi = true,
                includeQueries = setOf("foo", "bar")
            )
        ).generate()

        assertThat(codeGenResult.javaDataTypes.size).isEqualTo(2)

        assertThat(codeGenResult.javaDataTypes[0].typeSpec.name).isEqualTo("FooInput")
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs[0].name).isEqualTo("_public")
        assertThat(codeGenResult.javaDataTypes[0].typeSpec.fieldSpecs[0].initializer.toString()).isEqualTo("true")

        assertThat(codeGenResult.javaDataTypes[1].typeSpec.name).isEqualTo("BarInput")
        assertThat(codeGenResult.javaDataTypes[1].typeSpec.fieldSpecs[0].initializer.toString()).isEqualTo("")

        assertCompilesJava(codeGenResult.javaDataTypes)
    }

    @Test
    fun `generate client code for both query and subscription with same definitions`() {
        val schema = """
            type Query {
                shows: [Show]
            }

            type Subscription {
                shows: [Show]
            }
            
            type Mutation {
                shows: [Show]
            }

            type Show {
                id: Int
                title: String
            }
        """.trimIndent()

        val codeGenResult = CodeGen(
            CodeGenConfig(
                schemas = setOf(schema),
                packageName = basePackageName,
                generateClientApi = true
            )
        ).generate()

        assertThat(codeGenResult.javaQueryTypes.size).isEqualTo(3)
        assertThat(codeGenResult.javaQueryTypes[0].packageName).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.client.query")
        assertThat(codeGenResult.javaQueryTypes[1].packageName).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.client.subscription")
        assertThat(codeGenResult.javaQueryTypes[2].packageName).isEqualTo("com.netflix.graphql.dgs.codegen.tests.generated.client.mutation")

        assertCompilesJava(codeGenResult.javaQueryTypes)
    }
}
