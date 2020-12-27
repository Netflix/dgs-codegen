[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/gradle-netflixoss-project-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# DGS Code Generation Plugin

The DGS Code Generation plugin generates code for basic types and example data fetchers based on the your Domain Graph Service's graphql schema file during the project's build process.
The plugin requires the path to schema files and the package name to use to generate the file. If no schema path is specified, 
it will look under src/resources/schema for any files with .graphqls extension.  

# Quick Start

Update your project's build.gradle to apply the plugin:
```
// Using plugins DSL
plugins {
	id "com.netflix.dgs.codegen" version "4.0.10"
}
```
or 
````
// Using legacy plugin application
buildscript {
    dependencies{
        classpath 'com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-gradle:latest.release
    }
}

apply plugin: 'com.netflix.dgs.codegen'
````

GenerateJava is a gradle task that is run as part of your project's build to generate sources that your project depends on. 
Please ensure that your project's sources refer to the generated code using the specified package name.
 
To trigger code generation, add the following parameters for the generateJava task in your build.gradle:
 ````
generateJava{
  schemaPaths = ["${projectDir}/src/main/resources/schema"] // List of directories containing schema files
  packageName = 'com.example.packagename' // The package name to use to generate sources
}
 ````

# Generated Output
The generated types are available as part of the packageName.types package under build/generated. These are automatically added to your project's sources.
The generated example data fetchers are available under build/generated-examples. Note that these are NOT added to your project's sources and serve mainly as a 
basic boilerplate code requiring further customization.







