# Query.find: Result!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| filter |  | ✅ | Filter! |
            
## Example
```graphql
{
  find(filter: {mandatoryString : "randomString", optionalString : "randomString", mandatoryNumber : 6615423127894018818, optionalNumber : 2375643081499873615}) {
    isSuccessful
    result
  }
}

```