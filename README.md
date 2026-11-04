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

## Output directories

Use `outputDir` to configure the final directory for generated Java or Kotlin sources and `exampleOutputDir` for example data fetchers.
Both accept a `File` or a lazy `Provider<Directory>`.

```groovy
generateJava {
    outputDir = layout.buildDirectory.dir('custom-generated')
    exampleOutputDir = layout.buildDirectory.dir('custom-examples')
}
```

The plugin automatically adds `outputDir` to the main source set.
Without overrides, sources go to `build/generated/sources/dgs-codegen` and examples go to `build/generated/sources/dgs-codegen-generated-examples`.
These defaults follow `layout.buildDirectory`.

`generatedSourcesDir` is retained only for backward compatibility.
It configures a root directory, while `outputDir` and `exampleOutputDir` configure final directories.
To preserve the paths from `generatedSourcesDir = file('build/external').absolutePath`, use:

```groovy
generateJava {
    outputDir = layout.buildDirectory.dir('external/generated/sources/dgs-codegen')
    exampleOutputDir = layout.buildDirectory.dir('external/generated/sources/dgs-codegen-generated-examples')
}
```

Explicit output directories take precedence over `generatedSourcesDir`, including later changes to that root.

## Generated documentation

The Gradle plugin can generate Markdown documentation by setting `generateDocs = true` in the `generateJava` task configuration.
Documentation goes to `build/generated/docs/dgs-codegen` by default.
`outputDir` and `exampleOutputDir` do not move documentation.
For backward compatibility, its path remains `<generatedSourcesDir>/generated/docs/dgs-codegen`.
If an existing build uses a custom `generatedSourcesDir` and generates documentation, keep that setting to preserve the documentation path when migrating source outputs.
When documentation generation is disabled, this directory is not created.

# Contributing, asking questions and reporting issues.

Please read our [contributor guide](CONTRIBUTING.md)!


