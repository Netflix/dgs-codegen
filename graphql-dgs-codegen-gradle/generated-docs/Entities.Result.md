# Result - Federated Entities Query
### Query
```graphql
query ($representations: [_Any!]!) {
  entities(representations: $representations) {
    ... on Result {
      isSuccessful
      result
    }
  }
}

```

### Variables
```json
{"representations":[{"result":"randomString","__typename":"Result"}]}
```