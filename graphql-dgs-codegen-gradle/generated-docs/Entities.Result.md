# Result - Federated Entities Query
### Query
```graphql
query ($representations: [_Any!]!) {
  ... on Result {
    isSuccessful
    result
  }
}

```

### Variables
```json
{"representations":[{"result":"randomString","__typename":"Result"}]}
```