# Query.find: Result!
                 
## Arguments
| Name | Description | Required | Type |
| :--- | :---------- | :------: | :--: |
| filter |  | ✅ | Filter! |
            
## Example
```graphql
{
  find(filter: {mandatoryString : "randomString", optionalString : "randomString", mandatoryNumber : 4989624281559757994, optionalNumber : 2301060933645810468}) {
    isSuccessful
    result
  }
}

```