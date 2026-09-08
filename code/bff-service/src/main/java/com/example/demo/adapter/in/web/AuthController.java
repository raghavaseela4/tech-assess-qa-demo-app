package com.example.demo.adapter.in.web;

import com.example.bff.adapter.in.web.generated.AuthenticationApi;
import com.example.bff.adapter.in.web.generated.model.LoginRequestDTO;
import com.example.bff.adapter.in.web.generated.model.SignupRequestDTO;
import com.example.bff.adapter.in.web.generated.model.UserResponseDTO;
import com.example.bff.adapter.in.web.generated.model.UserRoleDTO;
import com.example.demo.application.auth.LoginCommand;
import com.example.demo.application.auth.LoginResult;
import com.example.demo.application.auth.LoginUseCase;
import com.example.demo.application.auth.LogoutUseCase;
import com.example.demo.application.auth.RefreshTokenUseCase;
import com.example.demo.application.auth.SignupCommand;
import com.example.demo.application.auth.SignupResult;
import com.example.demo.application.auth.SignupUseCase;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;

@RestController
public class AuthController implements AuthenticationApi {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final Duration ACCESS_COOKIE_MAX_AGE = Duration.ofMinutes(5);
    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofMinutes(30);

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final HttpServletRequest httpServletRequest;
    private final HttpServletResponse httpServletResponse;

    public AuthController(
            SignupUseCase signupUseCase,
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public ResponseEntity<UserResponseDTO> signup(SignupRequestDTO signupRequestDTO) {
        var command = new SignupCommand(
                signupRequestDTO.getEmail(),
                signupRequestDTO.getPassword(),
                signupRequestDTO.getName()
        );

        var result = signupUseCase.signup(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponseDTO(result));
    }

    @Override
    public ResponseEntity<UserResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        var command = new LoginCommand(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getPassword()
        );

        var result = loginUseCase.login(command);
        setTokenCookies(result.accessToken(), result.refreshToken());
        return ResponseEntity.ok(toUserResponseDTO(result));
    }

    @Override
    public ResponseEntity<Void> refresh() {
        var refreshToken = extractRefreshTokenFromCookies();

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var result = refreshTokenUseCase.refresh(refreshToken);

            setTokenCookies(result.accessToken(), result.refreshToken());
            return ResponseEntity.noContent().build();
        } catch (com.example.demo.application.auth.InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Override
    public ResponseEntity<Void> logout() {
        var accessToken = extractAccessTokenFromCookies();

        if (accessToken != null) {
            logoutUseCase.logout(accessToken);
        }

        var clearAccessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        var clearRefreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, clearAccessCookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString());

        return ResponseEntity.noContent().build();
    }

    private void setTokenCookies(String accessToken, String refreshToken) {
        var accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, accessToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(ACCESS_COOKIE_MAX_AGE)
                .build();

        var refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_COOKIE_MAX_AGE)
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

    private String extractAccessTokenFromCookies() {
        if (httpServletRequest.getCookies() == null) {
            return null;
        }
        return Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractRefreshTokenFromCookies() {
        if (httpServletRequest.getCookies() == null) {
            return null;
        }
        return Arrays.stream(httpServletRequest.getCookies())
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private UserResponseDTO toUserResponseDTO(SignupResult result) {
        return createUserResponseDTO(result.userId(), result.email(), result.name(), result.role(), result.createdAt());
    }

    private UserResponseDTO toUserResponseDTO(LoginResult result) {
        return createUserResponseDTO(result.userId(), result.email(), result.name(), result.role(), result.createdAt());
    }

    private UserResponseDTO createUserResponseDTO(
            java.util.UUID userId,
            String email,
            String name,
            String role,
            java.time.OffsetDateTime createdAt
    ) {
        var response = new UserResponseDTO();
        response.setUserId(userId);
        response.setEmail(email);
        response.setName(name);
        response.setRole(UserRoleDTO.fromValue(role));
        response.setCreatedAt(createdAt);
        return response;
    }
}
