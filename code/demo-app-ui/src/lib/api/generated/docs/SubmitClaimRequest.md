
# SubmitClaimRequest


## Properties

Name | Type
------------ | -------------
`incidentDate` | Date
`incidentLocation` | string
`description` | string
`claimAmount` | number

## Example

```typescript
import type { SubmitClaimRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "incidentDate": Sun Feb 15 08:00:00 SGT 2026,
  "incidentLocation": 123 Main St, Springfield, IL,
  "description": Water damage to basement due to pipe burst,
  "claimAmount": 5000.0,
} satisfies SubmitClaimRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SubmitClaimRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


