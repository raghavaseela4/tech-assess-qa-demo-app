package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.Encoding;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class ZipkinConfig {

    @Bean
    Encoding zipkinEncoding() {
        return Encoding.JSON;
    }

    @Bean
    BytesMessageSender zipkinSender(
            @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}") String endpoint) {
        return URLConnectionSender.create(endpoint);
    }
}
