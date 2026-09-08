# AdminDashboardApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**flushDashboardCache**](AdminDashboardApi.md#flushdashboardcache) | **POST** /api/admin/dashboard/flush-cache | Flush dashboard statistics cache |
| [**getDashboardStats**](AdminDashboardApi.md#getdashboardstats) | **GET** /api/admin/dashboard/stats | Get admin dashboard statistics |
| [**listAllClaims**](AdminDashboardApi.md#listallclaims) | **GET** /api/admin/claims | List all claims (admin) |
| [**listAllUsers**](AdminDashboardApi.md#listallusers) | **GET** /api/admin/users | List all users (admin) |
| [**updateClaimStatus**](AdminDashboardApi.md#updateclaimstatusoperation) | **PATCH** /api/admin/claims/{claimId}/status | Update the status of a claim |



## flushDashboardCache

> flushDashboardCache()

Flush dashboard statistics cache

Manually evicts the dashboard statistics cache. Requires admin role.

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { FlushDashboardCacheRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AdminDashboardApi(config);

  try {
    const data = await api.flushDashboardCache();
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

`void` (Empty response body)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Cache flushed successfully |  -  |
| **401** | Not authenticated |  -  |
| **403** | Insufficient permissions (admin role required) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getDashboardStats

> DashboardStatsResponse getDashboardStats(bypassCache)

Get admin dashboard statistics

Returns aggregated statistics for the admin dashboard. Results are cached in Redis. Use bypassCache&#x3D;true to force a fresh calculation. 

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { GetDashboardStatsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AdminDashboardApi(config);

  const body = {
    // boolean | When true, bypasses the Redis cache and fetches fresh data (optional)
    bypassCache: false,
  } satisfies GetDashboardStatsRequest;

  try {
    const data = await api.getDashboardStats(body);
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
| **bypassCache** | `boolean` | When true, bypasses the Redis cache and fetches fresh data | [Optional] [Defaults to `false`] |

### Return type

[**DashboardStatsResponse**](DashboardStatsResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Dashboard statistics |  -  |
| **401** | Not authenticated |  -  |
| **403** | Insufficient permissions (admin role required) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## listAllClaims

> Array&lt;ClaimResponse&gt; listAllClaims(status, userId)

List all claims (admin)

Returns all claims in the system. Supports filtering by status and userId.

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { ListAllClaimsRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AdminDashboardApi(config);

  const body = {
    // ClaimStatus | Filter claims by status (optional)
    status: SUBMITTED,
    // string | Filter claims by user ID (optional)
    userId: 550e8400-e29b-41d4-a716-446655440000,
  } satisfies ListAllClaimsRequest;

  try {
    const data = await api.listAllClaims(body);
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
| **status** | `ClaimStatus` | Filter claims by status | [Optional] [Defaults to `undefined`] [Enum: SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CLOSED] |
| **userId** | `string` | Filter claims by user ID | [Optional] [Defaults to `undefined`] |

### Return type

[**Array&lt;ClaimResponse&gt;**](ClaimResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of all claims |  -  |
| **401** | Not authenticated |  -  |
| **403** | Insufficient permissions (admin role required) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## listAllUsers

> Array&lt;UserResponse&gt; listAllUsers()

List all users (admin)

Returns all users in the system. Requires admin role.

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { ListAllUsersRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AdminDashboardApi(config);

  try {
    const data = await api.listAllUsers();
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

[**Array&lt;UserResponse&gt;**](UserResponse.md)

### Authorization

[keycloak accessCode](../README.md#keycloak-accessCode)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of all users |  -  |
| **401** | Not authenticated |  -  |
| **403** | Insufficient permissions (admin role required) |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## updateClaimStatus

> ClaimResponse updateClaimStatus(claimId, updateClaimStatusRequest)

Update the status of a claim

Allows an admin to update the status of a specific claim.

### Example

```ts
import {
  Configuration,
  AdminDashboardApi,
} from '';
import type { UpdateClaimStatusOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AdminDashboardApi(config);

  const body = {
    // string | The unique identifier of the claim
    claimId: 660e8400-e29b-41d4-a716-446655440001,
    // UpdateClaimStatusRequest
    updateClaimStatusRequest: {"newStatus":"APPROVED"},
  } satisfies UpdateClaimStatusOperationRequest;

  try {
    const data = await api.updateClaimStatus(body);
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
| **updateClaimStatusRequest** | [UpdateClaimStatusRequest](UpdateClaimStatusRequest.md) |  | |

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
| **200** | Claim status updated successfully |  -  |
| **400** | Invalid status transition |  -  |
| **401** | Not authenticated |  -  |
| **403** | Insufficient permissions (admin role required) |  -  |
| **404** | Claim not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

