[![CI](https://github.com/Netflix/dgs-codegen/actions/workflows/ci.yml/badge.svg)](https://github.com/Netflix/dgs-codegen/actions/workflows/ci.yml)
[![GitHub release](https://img.shields.io/github/v/release/Netflix/dgs-codegen.svg)](https://GitHub.com/Netflix/dgs-framework/releases)
[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/gradle-netflixoss-project-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# DGS Code Generation Plugin

The DGS Code Generation plugin generates code during your project’s build process based on your GraphQL schema file. The
plugin generates the following:

* Data types for types, input types, enums and interfaces.
* A DgsConstants class containing the names of types and fields
* A type safe query API that represents your queries
* Example data fetchers

# Getting Started

To get started with the DGS Framework the [getting started guide](https://netflix.github.io/dgs/getting-started/) is
great starting place. Documentation for DGS Code Generation can be
found [here](https://netflix.github.io/dgs/generating-code-from-schema/).

## Configuring the output directory

By default, generated sources are written to
`<generatedSourcesDir>/generated/sources/dgs-codegen`, which is `build/generated/sources/dgs-codegen` unless
`generatedSourcesDir` is changed. Set `outputDir` to write the generated sources to an exact directory instead, similar
to the `outputDir` setting of the OpenAPI Generator Gradle plugin:

```groovy
generateJava {
    outputDir = file("$buildDir/generated/sources/graphql")
    exampleOutputDir = file("$buildDir/generated/sources/graphql-examples")
    docsOutputDir = file("$buildDir/generated/docs/graphql")
}
```

`outputDir` takes precedence over `generatedSourcesDir`; builds that only set `generatedSourcesDir` keep the previous
layout. The same precedence applies to `exampleOutputDir` and `docsOutputDir`. Relative paths are resolved against the
project directory.

The setters also accept a `String` or a `Provider<Directory>`, which is useful from the Kotlin DSL:

```kotlin
tasks.named<com.netflix.graphql.dgs.codegen.gradle.GenerateJavaTask>("generateJava") {
    setOutputDir(layout.buildDirectory.dir("generated/sources/graphql"))
}
```

## Generated documentation

The Gradle plugin can generate Markdown documentation by setting `generateDocs = true` in the `generateJava` task
configuration. It writes the files to `<generatedSourcesDir>/generated/docs/dgs-codegen`; with the default
`generatedSourcesDir` of `build`, that is `build/generated/docs/dgs-codegen`. Changing `generatedSourcesDir` moves the
documentation along with generated source output. Set `docsOutputDir` to write the documentation to an exact directory
instead. When documentation generation is disabled, this directory is not created.

# Contributing, asking questions and reporting issues.

Please read our [contributor guide](CONTRIBUTING.md)!


