package com.example.demo.application.auth;

public interface RefreshTokenUseCase {
    RefreshTokenResult refresh(String refreshToken);
}
