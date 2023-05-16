# Query.find: Result!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| filter |  | ✅ | Filter! |
            
## Example
```graphql
{
  find(filter: {mandatoryString : "randomString", optionalString : "randomString", mandatoryNumber : 269109734867147797, optionalNumber : 6032492703890891451}) {
    isSuccessful
    result
  }
}

```