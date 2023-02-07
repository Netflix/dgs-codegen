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
