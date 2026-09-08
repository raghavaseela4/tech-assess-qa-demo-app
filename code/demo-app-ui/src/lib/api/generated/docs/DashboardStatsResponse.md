
# DashboardStatsResponse


## Properties

Name | Type
------------ | -------------
`totalUsers` | number
`totalClaims` | number
`claimsByStatus` | { [key: string]: number; }
`cacheHit` | boolean
`queryTimeMs` | number

## Example

```typescript
import type { DashboardStatsResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "totalUsers": 50,
  "totalClaims": 142,
  "claimsByStatus": {"SUBMITTED":45,"UNDER_REVIEW":32,"APPROVED":50,"REJECTED":15,"CLOSED":0},
  "cacheHit": true,
  "queryTimeMs": 12,
} satisfies DashboardStatsResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as DashboardStatsResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


