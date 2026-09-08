package com.example.demo.adapter.out.cache;

import com.example.demo.domain.dashboard.DashboardStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis cache adapter for dashboard statistics.
 * Implements graceful degradation: if Redis is unavailable,
 * returns empty Optional instead of throwing exceptions.
 */
@Component
public class DashboardStatsCache {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardStatsCache.class);
    private static final String CACHE_KEY = "dashboard:stats";

    private final RedisTemplate<String, DashboardStats> redisTemplate;

    public DashboardStatsCache(RedisTemplate<String, DashboardStats> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Retrieves dashboard stats from cache.
     *
     * @return Optional containing cached stats, or empty if cache miss or Redis unavailable
     */
    public Optional<DashboardStats> get() {
        try {
            DashboardStats stats = redisTemplate.opsForValue().get(CACHE_KEY);
            if (stats != null) {
                LOG.debug("Cache HIT for key: {}", CACHE_KEY);
                return Optional.of(stats);
            }
            LOG.debug("Cache MISS for key: {}", CACHE_KEY);
            return Optional.empty();
        } catch (RuntimeException exception) {
            LOG.warn("Redis unavailable, falling back to database. Error: {}", exception.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Stores dashboard stats in cache with TTL.
     *
     * @param stats the statistics to cache
     * @param ttlSeconds time-to-live in seconds
     */
    public void put(DashboardStats stats, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, stats, Duration.ofSeconds(ttlSeconds));
            LOG.debug("Cached dashboard stats with TTL: {}s", ttlSeconds);
        } catch (RuntimeException exception) {
            LOG.warn("Failed to cache dashboard stats. Error: {}", exception.getMessage());
        }
    }

    /**
     * Manually evicts cached dashboard stats.
     */
    public void evict() {
        try {
            Boolean deleted = redisTemplate.delete(CACHE_KEY);
            if (Boolean.TRUE.equals(deleted)) {
                LOG.info("Cache evicted for key: {}", CACHE_KEY);
            }
        } catch (RuntimeException exception) {
            LOG.warn("Failed to evict cache. Error: {}", exception.getMessage());
        }
    }
}
