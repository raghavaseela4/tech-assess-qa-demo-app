package com.example.demo.application.auth;

public interface LoginUseCase {
    LoginResult login(LoginCommand command);
}
