
# ProblemDetail


## Properties

Name | Type
------------ | -------------
`type` | string
`title` | string
`status` | number
`detail` | string
`instance` | string

## Example

```typescript
import type { ProblemDetail } from ''

// TODO: Update the object below with actual values
const example = {
  "type": https://api.demo-app.com/errors/validation-error,
  "title": Validation Error,
  "status": 400,
  "detail": Incident date cannot be in the future,
  "instance": /api/claims/123,
} satisfies ProblemDetail

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ProblemDetail
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


