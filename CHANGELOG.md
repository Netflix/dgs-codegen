# Unreleased

## Operation roots (breaking change)

Operation discovery now follows explicit `schema` declarations and `extend schema` declarations.
`Query` remains the default query root when no query entry is declared.
Conventional `Mutation` and `Subscription` roots apply only when the schema definition is implicit.
This can remove generated operation APIs or change constant values for consumers that depended on literal-name discovery.

- Java and Kotlin retain data classes previously generated for renamed roots such as `RootQuery` and `RootMutation`.
- Root constants now contain the declared type names, so `QUERY_TYPE` changes from `"Query"` to `"RootQuery"` for `schema { query: RootQuery }`, even when an ordinary type named `Query` also exists.
- Conventional mutation and subscription types omitted from an explicit schema are ordinary types and no longer produce operation constants or client operation classes.
  For example, `schema { query: Query }` with `type Mutation` no longer produces `MUTATION_TYPE` (`Mutation_TYPE` in Kotlin) or mutation client operations.
  Add `mutation: Mutation` to the schema definition if that type is intended to serve as the mutation root, or remove references to those generated operation APIs if it is an ordinary type.
- The public two-argument `filterIncludedInConfig` extension retains its literal-name behavior and is deprecated.
  Pass the parsed schema `Document` to the new three-argument overload to honor declared roots.
- Kotlin closure projections apply include options at operation roots and retain full selections when those types appear in nested fields or fragments.
  A root with a nonempty include set uses `<Root>NestedProjection` for nested selections, even when the include set covers every current field, so adding fields does not change the nested receiver type.
  Nested subscription roots always use this variant to retain `__typename` defaults.
  Name collisions with schema types add a numeric suffix starting at `2` before `Projection`, such as `QueryNested2Projection`.
  Update extension helpers used in nested selections to receive and return the nested projection type instead of `<Root>Projection`.
- With `generateClientApi = true` and `generateDataTypes = false`, input and enum types required by roots with empty include sets are now generated, since their client operations are generated too.
  Without client generation, only explicitly included operations contribute required types.
