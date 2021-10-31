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

package com.netflix.graphql.dgs

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CodegenGradlePluginSpringBootSmokeTest {

    @TempDir
    lateinit var projectDir: File

    private val graphQLSchema =
        """
            type Query {
                result: Result!
                find(filter: Filter!): Result!
            }
            
            type Result {
                isSuccessful: Boolean
                result: String
            }
            
            input Filter {
                mandatoryString: String!
                optionalString: String
                mandatoryNumber: Int!
                optionalNumber: Int
            }
            
            interface Audited {
                lastUpdated: DateTime
                lastUpdatedBy: String
            }
            
            interface MetaData implements Audited {
                lastUpdated: DateTime
                lastUpdatedBy: String
            }
            
            type JSONMetaData implements MetaData & Audited {
                data: JSON
                url: Url
                lastUpdated: DateTime
                lastUpdatedBy: String
            }
            
            type SimpleMetaData implements MetaData & Audited{
                data: [String]
                lastUpdated: DateTime
                lastUpdatedBy: String
            }
            
            scalar Date 
            scalar DateTime
            scalar Time
            scalar JSON
            scalar Url
            """.trimMargin()

    @Test
    fun `A SpringBoot project can use the generated Java`() {
        prepareBuildGraphQLSchema(graphQLSchema)

        prepareBuildGradleFile(
            """
                plugins {
                    id 'java'
                    id 'com.netflix.dgs.codegen'
                }
                
                 repositories {
                	mavenCentral()
                }
                
                 generateJava {
                     packageName = 'com.netflix.testproject.graphql'
                     typeMapping = [
                        DateTime:   "java.time.OffsetTime",
                        Time:       "java.time.OffsetDateTime",
                        Date:       "java.time.LocalDate",
                        JSON:       "java.lang.Object",
                        Url:        "java.net.URL"
                     ]
                     snakeCaseConstantNames = true
                 }
                 
                 dependencies {
                    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
                	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
                	implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")
                	testImplementation("org.springframework.boot:spring-boot-starter-test")
                }
                
                sourceCompatibility = 1.8
                targetCompatibility = 1.8

                test {
                    useJUnitPlatform()
                }
                // Need to disable the core conventions since the artifacts are not yet visible.
                codegen.clientCoreConventionsEnabled = false
            """.trimMargin()
        )

        writeProjectFile(
            "src/test/java/AppTest.java",
            """
                import org.springframework.context.annotation.Configuration;
                import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
                import org.springframework.boot.test.context.SpringBootTest;
                import org.junit.jupiter.api.Test;
                
                import com.netflix.testproject.graphql.types.Filter;
                import com.netflix.testproject.graphql.types.Result;
                import com.netflix.testproject.graphql.types.JSONMetaData;
                import com.netflix.testproject.graphql.DgsConstants;
                import com.netflix.testproject.graphql.DgsConstants.QUERY;
                import com.netflix.testproject.graphql.DgsConstants.RESULT;
                import com.netflix.testproject.graphql.DgsConstants.FILTER;
                import com.netflix.testproject.graphql.DgsConstants.JSON_META_DATA;
                import com.netflix.testproject.graphql.DgsConstants.SIMPLE_META_DATA;
                
                
                @SpringBootTest(
                    classes = {AppTest.TestConf.class},
                    properties = { "debug=true" }
                )
                @EnableAutoConfiguration
                public class AppTest {
                
                    @Test
                    public void test(){
                    }
                
                    @Configuration
                    static class TestConf { }
                }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(
                "--stacktrace",
                "--info",
                "generateJava",
                "check"
            ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(result.task(":check")).extracting { it?.outcome }.isEqualTo(SUCCESS)
    }

    @Test
    fun `A Spring Boot project can generate the generated Kotlin classes and objects`() {
        prepareBuildGradleFile(
            """
                plugins {
                    id 'java'
                    id 'org.jetbrains.kotlin.jvm' version '1.4.10' 
                    id 'com.netflix.dgs.codegen'
                }
                
                 repositories {
                	mavenCentral()
                }
                
                 generateJava {
                     packageName = 'com.netflix.testproject.graphql'
                     typeMapping = [
                        DateTime:   "java.time.OffsetTime",
                        Time:       "java.time.OffsetDateTime",
                        Date:       "java.time.LocalDate",
                        JSON:       "java.lang.Object",
                        Url:        "java.net.URL"
                     ]
                     language = "KOTLIN"
                     snakeCaseConstantNames = true
                 }
                 
                dependencies {
                    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:latest.release"))
                	implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
                	implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")
                	testImplementation("org.springframework.boot:spring-boot-starter-test")
                }
                
                sourceCompatibility = 1.8
                targetCompatibility = 1.8

                test {
                    useJUnitPlatform()
                }
                // Need to disable the core conventions since the artifacts are not yet visible.
                codegen.clientCoreConventionsEnabled = false
            """.trimMargin()
        )

        writeProjectFile(
            "src/test/kotlin/AppTest.kt",
            """
                import org.springframework.context.annotation.Configuration
                import org.springframework.boot.autoconfigure.EnableAutoConfiguration
                import org.springframework.boot.test.context.SpringBootTest
                import org.junit.jupiter.api.Test
                
                import com.netflix.testproject.graphql.types.Filter
                import com.netflix.testproject.graphql.types.Result
                import com.netflix.testproject.graphql.types.JSONMetaData
                import com.netflix.testproject.graphql.DgsConstants
                import com.netflix.testproject.graphql.DgsConstants.QUERY
                import com.netflix.testproject.graphql.DgsConstants.RESULT
                import com.netflix.testproject.graphql.DgsConstants.FILTER
                import com.netflix.testproject.graphql.DgsConstants.JSON_META_DATA
                import com.netflix.testproject.graphql.DgsConstants.SIMPLE_META_DATA
                
                
                @SpringBootTest(
                    classes=[AppTest.TestConf::class],
                    properties=["debug=true"]
                )
                @EnableAutoConfiguration
                internal class AppTest{
                
                    @Test
                    fun test() {}
                
                    @Configuration
                    open class TestConf { }
                }
            """.trimIndent()
        )

        prepareBuildGraphQLSchema(graphQLSchema)

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withDebug(true)
            .withArguments(
                "--stacktrace",
                "generateJava",
                "check"
            ).build()

        assertThat(result.task(":generateJava")).extracting { it?.outcome }.isEqualTo(SUCCESS)
        assertThat(result.task(":check")).extracting { it?.outcome }.isEqualTo(SUCCESS)
    }

    private fun prepareBuildGradleFile(content: String) {
        writeProjectFile("build.gradle", content)
    }

    private fun prepareBuildGraphQLSchema(content: String) {
        writeProjectFile("src/main/resources/schema/schema.graphql", content)
    }

    private fun writeProjectFile(relativePath: String, content: String) {
        val file = File(projectDir, relativePath)
        file.parentFile.mkdirs()
        file.writeText(content)
    }
}
