package com.example.demo.config;

import com.example.demo.adapter.in.websocket.ClaimWebSocketHandler;
import com.example.demo.adapter.in.websocket.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private final ClaimWebSocketHandler claimWebSocketHandler;
    private final JwtDecoder jwtDecoder;
    private final String allowedOrigins;

    public WebSocketConfig(
            ClaimWebSocketHandler claimWebSocketHandler,
            JwtDecoder jwtDecoder,
            @Value("${websocket.allowed-origins}") String allowedOrigins) {
        this.claimWebSocketHandler = claimWebSocketHandler;
        this.jwtDecoder = jwtDecoder;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(claimWebSocketHandler, "/api/ws/claims")
                .addInterceptors(new WebSocketAuthInterceptor(jwtDecoder))
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
