/*
 *
 *  Copyright 2026 Netflix, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FROM_CACHE
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.security.MessageDigest
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class GenerateJavaTaskBuildCacheTest {
    @Test
    fun schemaContentSwapsAcrossSameNamedRootsDoNotReuseStaleOutput(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val projectDir = createRootsProject(tempDir, "swapped-roots", cacheDir)
        writeRoots(projectDir, FIRST_EXTENSION, SECOND_EXTENSION)

        assertThat(run(projectDir, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")

        writeRoots(projectDir, SECOND_EXTENSION, FIRST_EXTENSION)
        assertThat(run(projectDir, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fb", "fa")

        val freshProject = createRootsProject(tempDir, "swapped-roots-fresh", File(tempDir, "unused-cache"))
        writeRoots(freshProject, SECOND_EXTENSION, FIRST_EXTENSION)
        assertThat(run(freshProject, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(outputFingerprints(generatedSources(projectDir))).isEqualTo(outputFingerprints(generatedSources(freshProject)))
    }

    @Test
    fun workspacesWithADifferentAbsolutePathOrderDoNotShareCachedOutput(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        // The same relative layout: the project's own schema plus a sibling `m-ext` root. `a-proj` sorts before
        // `m-ext` and `z-proj` sorts after it, so CodeGen concatenates the two roots in opposite order.
        val aProject = createWorkspace(File(tempDir, "ws-a"), "a-proj", cacheDir)
        val zProject = createWorkspace(File(tempDir, "ws-z"), "z-proj", cacheDir)
        val zFresh = createWorkspace(File(tempDir, "ws-z-fresh"), "z-proj", File(tempDir, "unused-cache"))

        assertThat(run(aProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(aProject)).containsExactly("id", "fp", "fe")

        assertThat(run(zProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(zProject)).containsExactly("id", "fe", "fp")

        assertThat(run(zFresh, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(outputFingerprints(generatedSources(zProject))).isEqualTo(outputFingerprints(generatedSources(zFresh)))
    }

    @Test
    fun reversingConfiguredSchemaRootsDoesNotChangeTheOutput(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val projectDir = createRootsProject(tempDir, "reversed-roots", cacheDir)
        writeRoots(projectDir, FIRST_EXTENSION, SECOND_EXTENSION)

        assertThat(run(projectDir, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        val original = outputFingerprints(generatedSources(projectDir))
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")

        run(projectDir, "--build-cache", "-PreverseRoots=true", "generateJava")
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")
        assertThat(outputFingerprints(generatedSources(projectDir))).isEqualTo(original)

        val freshProject = createRootsProject(tempDir, "reversed-roots-fresh", File(tempDir, "unused-cache"))
        writeRoots(freshProject, FIRST_EXTENSION, SECOND_EXTENSION)
        run(freshProject, "--no-build-cache", "-PreverseRoots=true", "generateJava")
        assertThat(outputFingerprints(generatedSources(freshProject))).isEqualTo(original)
    }

    @Test
    fun dependencyJarEntryOrderDoesNotChangeTheOutput(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val projectDir = createJarProject(tempDir, "jar-entry-order", cacheDir)
        writeSchemaJar(File(projectDir, "schemas.jar"), listOf("c-extension.graphqls", "a-base.graphqls", "b-extension.graphqls"))

        assertThat(run(projectDir, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fb", "fc")

        writeSchemaJar(File(projectDir, "schemas.jar"), listOf("a-base.graphqls", "b-extension.graphqls", "c-extension.graphqls"))
        run(projectDir, "--build-cache", "generateJava")
        assertThat(fooFields(projectDir)).containsExactly("base", "fb", "fc")

        val freshProject = createJarProject(tempDir, "jar-entry-order-fresh", File(tempDir, "unused-cache"))
        writeSchemaJar(File(freshProject, "schemas.jar"), listOf("a-base.graphqls", "b-extension.graphqls", "c-extension.graphqls"))
        assertThat(run(freshProject, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(outputFingerprints(generatedSources(projectDir))).isEqualTo(outputFingerprints(generatedSources(freshProject)))
    }

    @Test
    fun dependencyJarsWithADifferentAbsolutePathOrderDoNotShareCachedOutput(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")

        // The classpath order is always base.jar, then ext.jar. `a-proj/base.jar` sorts before `m-lib/ext.jar`,
        // and `m-lib/ext.jar` sorts before `z-proj/base.jar`, so CodeGen reads the jars in opposite order.
        fun workspace(
            name: String,
            projectName: String,
            cache: File,
        ): File {
            val projectDir = createJarProject(File(tempDir, name), projectName, cache, "base.jar", "../m-lib/ext.jar")
            writeJar(File(projectDir, "base.jar"), "a-base.graphqls" to BASE_SCHEMA + " extend type Foo { fp: String }")
            writeJar(File(projectDir, "../m-lib/ext.jar"), "ext.graphqls" to "extend type Foo { fe: String }")
            return projectDir
        }

        val aProject = workspace("ws-a", "a-proj", cacheDir)
        val zProject = workspace("ws-z", "z-proj", cacheDir)
        val zFresh = workspace("ws-z-fresh", "z-proj", File(tempDir, "unused-cache"))

        assertThat(run(aProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(aProject)).containsExactly("base", "fp", "fe")

        assertThat(run(zProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(zProject)).containsExactly("base", "fe", "fp")

        assertThat(run(zFresh, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(outputFingerprints(generatedSources(zProject))).isEqualTo(outputFingerprints(generatedSources(zFresh)))
    }

    @Test
    fun schemaProducedByAnEarlierTaskIsGeneratedUnderTheConfigurationCache(
        @TempDir tempDir: File,
    ) {
        val projectDir = createProducerProject(tempDir, "producer")
        File(projectDir, "swap.txt").writeText("plain")

        val first = runWithConfigurationCache(projectDir, "generateJava")
        assertThat(first.output).contains("Configuration cache entry stored.")
        assertThat(first.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")

        val second = runWithConfigurationCache(projectDir, "generateJava")
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":generateJava")?.outcome).isEqualTo(UP_TO_DATE)
    }

    @Test
    fun schemaFileAddedBetweenConfigurationCacheRunsIsPickedUp(
        @TempDir tempDir: File,
    ) {
        val projectDir = createRootsProject(tempDir, "added-file", File(tempDir, "unused-cache"))
        writeRoots(projectDir, FIRST_EXTENSION, SECOND_EXTENSION)

        assertThat(runWithConfigurationCache(projectDir, "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")

        File(projectDir, "schemas/base/added.graphqls").writeText("extend type Foo { fz: String }")
        val second = runWithConfigurationCache(projectDir, "generateJava")
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb", "fz")
    }

    @Test
    fun swapOfSameNamedFilesFromAProducerTaskReexecutesUnderTheConfigurationCache(
        @TempDir tempDir: File,
    ) {
        val projectDir = createProducerProject(tempDir, "producer-swap")
        File(projectDir, "swap.txt").writeText("plain")

        assertThat(runWithConfigurationCache(projectDir, "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fa", "fb")

        File(projectDir, "swap.txt").writeText("swap")
        val second = runWithConfigurationCache(projectDir, "generateJava")
        assertThat(second.output).contains("Reusing configuration cache.")
        assertThat(second.task(":produce")?.outcome).isEqualTo(SUCCESS)
        assertThat(second.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base", "fb", "fa")
    }

    @Test
    fun unbuiltSchemaProjectDependencyWorksUnderTheConfigurationCache(
        @TempDir tempDir: File,
    ) {
        val projectDir = File(tempDir, "project-dependency").also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText("include 'schemas'")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'java'
                id 'com.netflix.dgs.codegen'
            }

            codegen.clientCoreConventionsEnabled = false

            dependencies {
                dgsCodegen project(':schemas')
            }

            generateJava {
                schemaPaths = []
                packageName = 'com.netflix.testproject.graphql'
                generatedSourcesDir = file('build/graphql').absolutePath
            }
            """.trimIndent(),
        )
        File(projectDir, "schemas/src/main/resources").mkdirs()
        File(projectDir, "schemas/build.gradle").writeText("plugins { id 'java' }")
        File(projectDir, "schemas/src/main/resources/schema.graphqls").writeText(BASE_SCHEMA)

        val first = runWithConfigurationCache(projectDir, "generateJava")
        assertThat(first.output).contains("Configuration cache entry stored.")
        assertThat(first.task(":schemas:jar")?.outcome).isEqualTo(SUCCESS)
        assertThat(first.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        assertThat(fooFields(projectDir)).containsExactly("base")
    }

    @Test
    fun derivesOutputProvidersFromGeneratedSourcesDir(
        @TempDir tempDir: File,
    ) {
        val outputRoot = "build/custom-output"
        val projectDir = createProject(tempDir, "derived-output-providers", outputRoot, File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            tasks.register('verifyGeneratedDirectories') {
                doLast {
                    def codegen = tasks.named('generateJava').get()
                    assert codegen.generatedSourcesDirectory.get().asFile == file('$outputRoot/generated/sources/dgs-codegen')
                    assert codegen.generatedExamplesDirectory.get().asFile == file('$outputRoot/generated/sources/dgs-codegen-generated-examples')
                    assert codegen.generatedDocsDirectory.get().asFile == file('$outputRoot/generated/docs/dgs-codegen')
                }
            }
            """.trimIndent(),
        )

        assertThat(run(projectDir, "verifyGeneratedDirectories").task(":verifyGeneratedDirectories")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun reusesCachedOutputsAcrossProjectAndOutputDirectories(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val firstProject = createProject(tempDir, "first-project", "build/first-output", cacheDir)
        val secondProject = createProject(tempDir, "second-project", "build/second-output", cacheDir)

        assertThat(run(firstProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(SUCCESS)
        val firstOutput = generatedOutput(firstProject, "build/first-output")
        val firstDocs = generatedDocsOutput(firstProject, "build/first-output")
        assertThat(firstDocs).isDirectory
        assertThat(outputFingerprints(firstDocs)).isNotEmpty

        assertThat(run(secondProject, "--build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(FROM_CACHE)
        val secondOutput = generatedOutput(secondProject, "build/second-output")
        val secondDocs = generatedDocsOutput(secondProject, "build/second-output")

        assertThat(outputFingerprints(firstOutput)).isEqualTo(outputFingerprints(secondOutput))
        assertThat(secondDocs).isDirectory
        assertThat(outputFingerprints(firstDocs)).isEqualTo(outputFingerprints(secondDocs))
    }

    @Test
    fun doesNotCreateGeneratedDocsDirectoryWhenDocsAreDisabled(
        @TempDir tempDir: File,
    ) {
        val outputRoot = "build/output"
        val projectDir = createProject(tempDir, "docs-disabled", outputRoot, File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            generateJava {
                generateDocs = false
            }
            """.trimIndent(),
        )

        assertSuccessfulExecution(run(projectDir, "--no-build-cache", "generateJava"))

        assertThat(generatedDocsOutput(projectDir, outputRoot)).doesNotExist()
    }

    @Test
    fun resolvesGeneratedDocsDirectoryWhenDocsAreDisabled(
        @TempDir tempDir: File,
    ) {
        val outputRoot = "build/output"
        val projectDir = createProject(tempDir, "docs-disabled-provider", outputRoot, File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            generateJava {
                generateDocs = false
            }

            def docsDirectory = tasks.named('generateJava').get().generatedDocsDirectory
            def expectedDocsDirectory = file('$outputRoot/generated/docs/dgs-codegen')
            tasks.register('verifyDocsDirectory') {
                doLast {
                    assert docsDirectory.get().asFile == expectedDocsDirectory
                }
            }
            """.trimIndent(),
        )

        assertThat(run(projectDir, "verifyDocsDirectory").task(":verifyDocsDirectory")?.outcome).isEqualTo(SUCCESS)
        assertThat(generatedDocsOutput(projectDir, outputRoot)).doesNotExist()
    }

    @Test
    fun removesStaleDocsWhenDocsAreToggledOffAndRestoresThemWhenToggledBackOn(
        @TempDir tempDir: File,
    ) {
        val outputRoot = "build/output"
        val projectDir = createProject(tempDir, "docs-toggle", outputRoot, File(tempDir, "toggle-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            generateJava {
                generateDocs = !providers.gradleProperty('docsOff').isPresent()
            }
            """.trimIndent(),
        )
        val docs = generatedDocsOutput(projectDir, outputRoot)

        assertSuccessfulExecution(run(projectDir, "--build-cache", "generateJava"))
        assertThat(docs).isDirectory
        val fingerprints = outputFingerprints(docs)
        assertThat(fingerprints).isNotEmpty

        assertSuccessfulExecution(run(projectDir, "--build-cache", "-PdocsOff=true", "generateJava"))
        assertThat(docs).doesNotExist()

        val restored = run(projectDir, "--build-cache", "generateJava")
        assertThat(restored.task(":generateJava")?.outcome).isEqualTo(FROM_CACHE)
        assertThat(restored.output).doesNotContain("Gradle does not know how file")
        assertThat(docs).isDirectory
        assertThat(outputFingerprints(docs)).isEqualTo(fingerprints)
    }

    @Test
    fun tracksSchemaAndConfigurationInputsAndUnchangedRuns(
        @TempDir tempDir: File,
    ) {
        val projectDir = createProject(tempDir, "input-tracking", "build/output", File(tempDir, "unused-cache"))
        File(projectDir, "build.gradle").appendText(
            """

            generateJava {
                packageName = providers.gradleProperty('testPackage').getOrElse('com.netflix.testproject.graphql')
                generateClient = providers.gradleProperty('testClient').map { it.toBoolean() }.getOrElse(false)
                typeMapping = providers.gradleProperty('testMapping').isPresent() ? [Date: 'java.time.Instant'] : [Date: 'java.time.LocalDateTime']
                includeQueries = providers.gradleProperty('testQueries').isPresent() ? ['hello'] : []
            }
            """.trimIndent(),
        )

        assertSuccessfulExecution(run(projectDir, "--no-build-cache", "generateJava"))
        assertThat(run(projectDir, "--no-build-cache", "generateJava").task(":generateJava")?.outcome).isEqualTo(UP_TO_DATE)

        assertSuccessfulExecution(run(projectDir, "--no-build-cache", "-PtestPackage=com.netflix.changed", "generateJava"))
        assertSuccessfulExecution(
            run(projectDir, "--no-build-cache", "-PtestPackage=com.netflix.changed", "-PtestClient=true", "generateJava"),
        )
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "generateJava",
            ),
        )
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "-PtestQueries=true",
                "generateJava",
            ),
        )

        File(projectDir, "src/main/resources/schema/schema.graphqls").appendText("\n# invalidates the schema snapshot\n")
        assertSuccessfulExecution(
            run(
                projectDir,
                "--no-build-cache",
                "-PtestPackage=com.netflix.changed",
                "-PtestClient=true",
                "-PtestMapping=true",
                "-PtestQueries=true",
                "generateJava",
            ),
        )
    }

    @Test
    fun doesNotCacheTimestampedGeneratedAnnotations(
        @TempDir tempDir: File,
    ) {
        val cacheDir = File(tempDir, "shared-build-cache")
        val firstProject = createProject(tempDir, "first-dated-project", "build/output", cacheDir)
        val secondProject = createProject(tempDir, "second-dated-project", "build/output", cacheDir)
        val datedConfiguration =
            """

            generateJava {
                generatedAnnotationType = 'jakarta.annotation.Generated'
                disableDatesInGeneratedAnnotation = false
            }
            """.trimIndent()
        File(firstProject, "build.gradle").appendText(datedConfiguration)
        File(secondProject, "build.gradle").appendText(datedConfiguration)

        assertSuccessfulExecution(run(firstProject, "--build-cache", "generateJava"))
        assertSuccessfulExecution(run(secondProject, "--build-cache", "generateJava"))
    }

    private fun createProject(
        parent: File,
        name: String,
        outputRoot: String,
        cacheDir: File,
    ): File {
        val sourceDir = File("src/test/resources/test-project")
        val projectDir = File(parent, name).also { it.mkdirs() }
        val buildFile = File(sourceDir, "build.gradle").readText()
        File(projectDir, "build.gradle").writeText(
            buildFile.replace(
                "generatedSourcesDir = \"\${projectDir}/build/graphql\"",
                "generatedSourcesDir = file('$outputRoot').absolutePath",
            ),
        )
        File(sourceDir, "settings.gradle").copyTo(File(projectDir, "settings.gradle"))
        File(projectDir, "settings.gradle").appendText(
            """

            buildCache {
                local {
                    directory = file('${cacheDir.invariantSeparatorsPath}')
                }
            }
            """.trimIndent(),
        )
        File(sourceDir, "src").copyRecursively(File(projectDir, "src"))
        return projectDir
    }

    private fun settingsFile(cacheDir: File): String =
        """
        buildCache {
            local {
                directory = file('${cacheDir.invariantSeparatorsPath}')
            }
        }
        """.trimIndent()

    private fun createRootsProject(
        parent: File,
        name: String,
        cacheDir: File,
    ): File {
        val projectDir = File(parent, name).also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText(settingsFile(cacheDir))
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'com.netflix.dgs.codegen'
            }

            codegen.clientCoreConventionsEnabled = false

            generateJava {
                schemaPaths = project.hasProperty('reverseRoots') ?
                    [file('schemas/b'), file('schemas/a'), file('schemas/base')] :
                    [file('schemas/base'), file('schemas/a'), file('schemas/b')]
                packageName = 'com.netflix.testproject.graphql'
                generatedSourcesDir = file('build/graphql').absolutePath
            }
            """.trimIndent(),
        )
        return projectDir
    }

    private fun writeRoots(
        projectDir: File,
        aSchema: String,
        bSchema: String,
    ) {
        listOf("a" to aSchema, "b" to bSchema, "base" to BASE_SCHEMA).forEach { (root, schema) ->
            File(projectDir, "schemas/$root").mkdirs()
            File(projectDir, "schemas/$root/${if (root == "base") "base" else "same"}.graphqls").writeText(schema)
        }
    }

    private fun createWorkspace(
        workspace: File,
        projectName: String,
        cacheDir: File,
    ): File {
        val projectDir = File(workspace, projectName).also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText(settingsFile(cacheDir))
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'com.netflix.dgs.codegen'
            }

            codegen.clientCoreConventionsEnabled = false

            generateJava {
                schemaPaths = [file('schema'), file('../m-ext')]
                packageName = 'com.netflix.testproject.graphql'
                generatedSourcesDir = file('build/graphql').absolutePath
            }
            """.trimIndent(),
        )
        File(projectDir, "schema").mkdirs()
        File(projectDir, "schema/base.graphqls").writeText("type Query { foo: Foo } type Foo { id: ID } extend type Foo { fp: String }")
        File(workspace, "m-ext").mkdirs()
        File(workspace, "m-ext/ext.graphqls").writeText("extend type Foo { fe: String }")
        return projectDir
    }

    private fun createJarProject(
        parent: File,
        name: String,
        cacheDir: File,
        vararg jars: String = arrayOf("schemas.jar"),
    ): File {
        val projectDir = File(parent, name).also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText(settingsFile(cacheDir))
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'com.netflix.dgs.codegen'
            }

            codegen.clientCoreConventionsEnabled = false

            dependencies {
                dgsCodegen files(${jars.joinToString { "'$it'" }})
            }

            generateJava {
                schemaPaths = []
                packageName = 'com.netflix.testproject.graphql'
                generatedSourcesDir = file('build/graphql').absolutePath
            }
            """.trimIndent(),
        )
        return projectDir
    }

    /** The schema roots are written by `produce`, so they do not exist until generateJava's first execution. */
    private fun createProducerProject(
        parent: File,
        name: String,
    ): File {
        val projectDir = File(parent, name).also { it.mkdirs() }
        File(projectDir, "settings.gradle").writeText("")
        File(projectDir, "build.gradle").writeText(
            """
            plugins {
                id 'com.netflix.dgs.codegen'
            }

            codegen.clientCoreConventionsEnabled = false

            def swapFile = layout.projectDirectory.file('swap.txt')
            def gen = layout.buildDirectory.dir('gen')
            tasks.register('produce') {
                inputs.file(swapFile)
                outputs.dir(gen)
                doLast {
                    def swap = swapFile.asFile.text.trim() == 'swap'
                    def dir = gen.get().asFile
                    ['a', 'b', 'base'].each { new File(dir, it).mkdirs() }
                    new File(dir, 'base/base.graphqls').text = '$BASE_SCHEMA'
                    new File(dir, 'a/same.graphqls').text = swap ? '$SECOND_EXTENSION' : '$FIRST_EXTENSION'
                    new File(dir, 'b/same.graphqls').text = swap ? '$FIRST_EXTENSION' : '$SECOND_EXTENSION'
                }
            }

            generateJava {
                dependsOn 'produce'
                schemaPaths = ['base', 'a', 'b'].collect { gen.get().dir(it).asFile }
                packageName = 'com.netflix.testproject.graphql'
                generatedSourcesDir = file('build/graphql').absolutePath
            }
            """.trimIndent(),
        )
        return projectDir
    }

    private fun writeSchemaJar(
        jar: File,
        entryOrder: List<String>,
    ) {
        val schemas =
            mapOf(
                "a-base.graphqls" to BASE_SCHEMA,
                "b-extension.graphqls" to SECOND_EXTENSION,
                "c-extension.graphqls" to THIRD_EXTENSION,
            )
        writeJar(jar, *entryOrder.map { it to schemas.getValue(it) }.toTypedArray())
    }

    private fun writeJar(
        jar: File,
        vararg entries: Pair<String, String>,
    ) {
        jar.parentFile.mkdirs()
        JarOutputStream(jar.outputStream()).use { out ->
            entries.forEach { (name, content) ->
                out.putNextEntry(ZipEntry(name))
                out.write(content.toByteArray())
                out.closeEntry()
            }
        }
    }

    private fun generatedSources(projectDir: File): File = File(projectDir, "build/graphql/generated/sources/dgs-codegen")

    /** The field names of the generated `Foo`, in declaration order (the nested builder repeats them). */
    private fun fooFields(projectDir: File): List<String> =
        Regex("""private \w+ (\w+);""")
            .findAll(File(generatedSources(projectDir), "com/netflix/testproject/graphql/types/Foo.java").readText())
            .map { it.groupValues[1] }
            .distinct()
            .toList()

    private fun runWithConfigurationCache(
        projectDir: File,
        vararg arguments: String,
    ): BuildResult = run(projectDir, "--configuration-cache", "--configuration-cache-problems=fail", *arguments)

    private fun run(
        projectDir: File,
        vararg arguments: String,
    ): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("--stacktrace", *arguments)
            .build()

    private fun assertSuccessfulExecution(result: BuildResult) {
        assertThat(result.task(":generateJava")?.outcome).isEqualTo(SUCCESS)
    }

    private fun generatedOutput(
        projectDir: File,
        outputRoot: String,
    ): File = File(projectDir, "$outputRoot/generated/sources/dgs-codegen")

    private fun generatedDocsOutput(
        projectDir: File,
        outputRoot: String,
    ): File = File(projectDir, "$outputRoot/generated/docs/dgs-codegen")

    private fun outputFingerprints(directory: File): Map<String, String> =
        directory
            .walkTopDown()
            .filter(File::isFile)
            .associate { file ->
                file.relativeTo(directory).invariantSeparatorsPath to
                    MessageDigest
                        .getInstance("SHA-256")
                        .digest(file.readBytes())
                        .joinToString("") { "%02x".format(it) }
            }

    private companion object {
        const val BASE_SCHEMA = "type Query { foo: Foo } type Foo { base: String }"
        const val FIRST_EXTENSION = "extend type Foo { fa: String }"
        const val SECOND_EXTENSION = "extend type Foo { fb: String }"
        const val THIRD_EXTENSION = "extend type Foo { fc: String }"
    }
}
