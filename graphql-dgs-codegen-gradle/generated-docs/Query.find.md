# Query.find: Result!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| filter |  | ✅ | Filter! |
            
## Example
```graphql
{
  find(filter: {mandatoryString : "randomString", optionalString : "randomString", mandatoryNumber : 197539653235116614, optionalNumber : 5816126044854263270}) {
    isSuccessful
    result
  }
}

```