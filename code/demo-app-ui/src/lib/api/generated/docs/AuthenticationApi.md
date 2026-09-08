# AuthenticationApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**login**](AuthenticationApi.md#loginoperation) | **POST** /api/auth/login | Authenticate a user |
| [**logout**](AuthenticationApi.md#logout) | **POST** /api/auth/logout | Log out the current user |
| [**refresh**](AuthenticationApi.md#refresh) | **POST** /api/auth/refresh | Refresh authentication tokens |
| [**signup**](AuthenticationApi.md#signupoperation) | **POST** /api/auth/signup | Register a new user account |



## login

> UserResponse login(loginRequest)

Authenticate a user

Authenticates a user with email and password. On success, returns a 200 with the authentication token set as an HTTP-only cookie. 

### Example

```ts
import {
  Configuration,
  AuthenticationApi,
} from '';
import type { LoginOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthenticationApi();

  const body = {
    // LoginRequest
    loginRequest: {"email":"jane.doe@example.com","password":"SecurePass123!"},
  } satisfies LoginOperationRequest;

  try {
    const data = await api.login(body);
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
| **loginRequest** | [LoginRequest](LoginRequest.md) |  | |

### Return type

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Login successful. Token returned in HTTP-only cookie. |  * Set-Cookie - HTTP-only cookie containing the authentication token <br>  |
| **401** | Invalid credentials |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## logout

> logout()

Log out the current user

Invalidates the current user\&#39;s session and clears the authentication cookie.

### Example

```ts
import {
  Configuration,
  AuthenticationApi,
} from '';
import type { LogoutRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // To configure OAuth2 access token for authorization: keycloak accessCode
    accessToken: "YOUR ACCESS TOKEN",
  });
  const api = new AuthenticationApi(config);

  try {
    const data = await api.logout();
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
- **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Logout successful. Authentication cookie cleared. |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## refresh

> refresh()

Refresh authentication tokens

Uses the refresh token cookie to obtain new access and refresh tokens. Both tokens are returned as HTTP-only cookies. 

### Example

```ts
import {
  Configuration,
  AuthenticationApi,
} from '';
import type { RefreshRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthenticationApi();

  try {
    const data = await api.refresh();
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

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **204** | Tokens refreshed successfully. New tokens set in HTTP-only cookies. |  * Set-Cookie - HTTP-only cookies containing new access and refresh tokens <br>  |
| **401** | Invalid or expired refresh token |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## signup

> UserResponse signup(signupRequest)

Register a new user account

Creates a new user account with the provided email, password, and name.

### Example

```ts
import {
  Configuration,
  AuthenticationApi,
} from '';
import type { SignupOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new AuthenticationApi();

  const body = {
    // SignupRequest
    signupRequest: {"email":"jane.doe@example.com","password":"SecurePass123!","name":"Jane Doe"},
  } satisfies SignupOperationRequest;

  try {
    const data = await api.signup(body);
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
| **signupRequest** | [SignupRequest](SignupRequest.md) |  | |

### Return type

[**UserResponse**](UserResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | User registered successfully |  -  |
| **400** | Validation error |  -  |
| **409** | Email already exists |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

