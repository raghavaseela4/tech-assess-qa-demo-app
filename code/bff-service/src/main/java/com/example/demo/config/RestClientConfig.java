package com.example.demo.config;

import com.example.bff.adapter.out.claims.ApiClient;
import com.example.demo.adapter.out.http.CorrelationIdInterceptor;
import com.example.demo.adapter.out.http.TracingInterceptor;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for REST clients with observability features.
 *
 * <p>Configures the OpenAPI-generated ApiClient with:
 * <ul>
 *   <li>Correlation ID propagation via CorrelationIdInterceptor</li>
 *   <li>Reasonable timeouts for production use</li>
 *   <li>Base path from application properties</li>
 * </ul>
 */
@Configuration
public class RestClientConfig {

    @Bean
    @ConditionalOnMissingBean
    public ApiClient claimsApiClient(
            @Value("${claims-service.url}") String claimsServiceUrl,
            CorrelationIdInterceptor correlationIdInterceptor,
            TracingInterceptor tracingInterceptor
    ) {
        // Create OkHttpClient with correlation ID and tracing interceptors
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(correlationIdInterceptor)
                .addInterceptor(tracingInterceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Configure ApiClient
        ApiClient apiClient = new ApiClient();
        apiClient.setHttpClient(httpClient);
        apiClient.setBasePath(claimsServiceUrl);

        return apiClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
