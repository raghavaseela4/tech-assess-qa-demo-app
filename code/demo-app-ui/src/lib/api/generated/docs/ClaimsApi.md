# ClaimsApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getClaim**](ClaimsApi.md#getclaim) | **GET** /api/claims/{claimId} | Get details of a specific claim |
| [**listClaims**](ClaimsApi.md#listclaims) | **GET** /api/claims | List the authenticated user\&#39;s claims |
| [**submitClaim**](ClaimsApi.md#submitclaimoperation) | **POST** /api/claims | Submit a new insurance claim |



## getClaim

> ClaimResponse getClaim(claimId)

Get details of a specific claim

Returns the details of a claim by its ID. Users can only access their own claims.

### Example

```ts
import {
  Configuration,
  ClaimsApi,
} from '';
import type { GetClaimRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new ClaimsApi(config);

  const body = {
    // string | The unique identifier of the claim
    claimId: 660e8400-e29b-41d4-a716-446655440001,
  } satisfies GetClaimRequest;

  try {
    const data = await api.getClaim(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **claimId** | `string` | The unique identifier of the claim | [Defaults to `undefined`] |

### Return type

[**ClaimResponse**](ClaimResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Claim details |  -  |
| **401** | Not authenticated |  -  |
| **403** | Forbidden - claim belongs to different user |  -  |
| **404** | Claim not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## listClaims

> Array&lt;ClaimSummaryResponse&gt; listClaims()

List the authenticated user\&#39;s claims

Returns all claims belonging to the currently authenticated user.

### Example

```ts
import {
  Configuration,
  ClaimsApi,
} from '';
import type { ListClaimsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new ClaimsApi(config);

  try {
    const data = await api.listClaims();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**Array&lt;ClaimSummaryResponse&gt;**](ClaimSummaryResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of user\&#39;s claims |  -  |
| **401** | Not authenticated |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## submitClaim

> ClaimResponse submitClaim(submitClaimRequest)

Submit a new insurance claim

Creates a new insurance claim for the authenticated user.

### Example

```ts
import {
  Configuration,
  ClaimsApi,
} from '';
import type { SubmitClaimOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new ClaimsApi(config);

  const body = {
    // SubmitClaimRequest
    submitClaimRequest: {"incidentDate":"2026-02-15","description":"Water damage to basement due to pipe burst"},
  } satisfies SubmitClaimOperationRequest;

  try {
    const data = await api.submitClaim(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **submitClaimRequest** | [SubmitClaimRequest](SubmitClaimRequest.md) |  | |

### Return type

[**ClaimResponse**](ClaimResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Claim submitted successfully |  -  |
| **400** | Validation error |  -  |
| **401** | Not authenticated |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

