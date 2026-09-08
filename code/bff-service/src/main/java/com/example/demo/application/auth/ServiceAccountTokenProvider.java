package com.example.demo.application.auth;

public interface ServiceAccountTokenProvider {

    String getServiceAccountToken();

    String forceRefreshToken();
}
