
# UserResponse


## Properties

Name | Type
------------ | -------------
`userId` | string
`email` | string
`name` | string
`role` | [UserRole](UserRole.md)
`createdAt` | Date

## Example

```typescript
import type { UserResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "userId": 550e8400-e29b-41d4-a716-446655440000,
  "email": jane.doe@example.com,
  "name": Jane Doe,
  "role": null,
  "createdAt": 2026-02-17T10:30Z,
} satisfies UserResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as UserResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


