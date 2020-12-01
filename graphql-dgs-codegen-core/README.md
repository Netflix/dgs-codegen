DGS CodeGen
===

This module generates Java/Kotlin code from Graphql schemas.

The generated code includes the following:

* "data classes", which represents your Graphql types, and are typically dumb POJOs. You will typically use the generated types directly in your project.
* DGS starter code, this includes example data fetchers to get you started, but you're typically required to add additional logic. 

Getting started
---

The codegen module can be used in three ways:

* As a Gradle plugin
* As a standalone CLI
* As a module, integrated in your own tooling (less common)

The Gradle plugin has the benefit that it can run automatically anytime your schema changes, to keep your data classes in sync with your schema.
This will be the most hands-off model, and is recommended for most DGS users.

Gradle plugin
----

TBD

CLI
---

