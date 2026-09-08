
# ClaimSummaryResponse


## Properties

Name | Type
------------ | -------------
`claimId` | string
`incidentDate` | Date
`description` | string
`claimAmount` | number
`status` | [ClaimStatus](ClaimStatus.md)
`createdAt` | Date

## Example

```typescript
import type { ClaimSummaryResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "claimId": 660e8400-e29b-41d4-a716-446655440001,
  "incidentDate": Sun Feb 15 08:00:00 SGT 2026,
  "description": Water damage to basement due to pipe burst,
  "claimAmount": 5000.0,
  "status": null,
  "createdAt": 2026-02-17T10:30Z,
} satisfies ClaimSummaryResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ClaimSummaryResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


