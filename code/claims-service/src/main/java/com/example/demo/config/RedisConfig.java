package com.example.demo.config;

import com.example.demo.domain.dashboard.DashboardStats;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for dashboard statistics caching.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, DashboardStats> dashboardStatsRedisTemplate(
            RedisConnectionFactory connectionFactory,
            tools.jackson.databind.ObjectMapper objectMapper) {
        RedisTemplate<String, DashboardStats> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJacksonJsonRedisSerializer(objectMapper));
        return template;
    }
}
