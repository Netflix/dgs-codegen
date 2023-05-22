# Query.find: Result!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| filter |  | ✅ | Filter! |
            
## Example
```graphql
{
  find(filter: {mandatoryString : "randomString", optionalString : "randomString", mandatoryNumber : 123456789, optionalNumber : 123456789}) {
    isSuccessful
    result
  }
}

```