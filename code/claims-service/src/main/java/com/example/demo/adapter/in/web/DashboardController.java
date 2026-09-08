package com.example.demo.adapter.in.web;

import com.example.claims.adapter.in.web.generated.DashboardApi;
import com.example.claims.adapter.in.web.generated.model.DashboardStatsResponseDTO;
import com.example.demo.adapter.out.cache.DashboardStatsCache;
import com.example.demo.application.usecases.GetDashboardStatsQuery;
import com.example.demo.application.usecases.GetDashboardStatsUseCase;
import com.example.demo.domain.dashboard.DashboardStats;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for admin dashboard endpoints.
 * Coordinates caching strategy and delegates business logic to use case.
 */
@RestController
public class DashboardController implements DashboardApi {

    private static final long CACHE_TTL_SECONDS = 300;

    private final GetDashboardStatsUseCase getDashboardStatsUseCase;
    private final DashboardStatsCache dashboardStatsCache;

    public DashboardController(GetDashboardStatsUseCase getDashboardStatsUseCase,
                               DashboardStatsCache dashboardStatsCache) {
        this.getDashboardStatsUseCase = getDashboardStatsUseCase;
        this.dashboardStatsCache = dashboardStatsCache;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsResponseDTO> getDashboardStats(Optional<Boolean> bypassCache) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        DashboardStats stats;

        boolean shouldBypassCache = bypassCache.orElse(false);

        if (!shouldBypassCache) {
            var cachedStats = dashboardStatsCache.get();
            if (cachedStats.isPresent()) {
                stats = cachedStats.get();
                cacheHit = true;
            } else {
                stats = getDashboardStatsUseCase.execute(new GetDashboardStatsQuery(false));
                dashboardStatsCache.put(stats, CACHE_TTL_SECONDS);
            }
        } else {
            stats = getDashboardStatsUseCase.execute(new GetDashboardStatsQuery(true));
        }

        long queryTimeMs = System.currentTimeMillis() - startTime;

        DashboardStatsResponseDTO response = mapToResponse(stats, cacheHit, queryTimeMs);
        return ResponseEntity.ok(response);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> flushDashboardCache() {
        dashboardStatsCache.evict();
        return ResponseEntity.noContent().build();
    }

    private DashboardStatsResponseDTO mapToResponse(DashboardStats stats, boolean cacheHit, long queryTimeMs) {
        Map<String, Integer> claimsByStatusMap = new HashMap<>();
        stats.claimsByStatus().forEach((status, count) ->
            claimsByStatusMap.put(status.name(), count)
        );

        return new DashboardStatsResponseDTO()
                .totalUsers(stats.totalUsers())
                .totalClaims(stats.totalClaims())
                .claimsByStatus(claimsByStatusMap)
                .cacheHit(cacheHit)
                .queryTimeMs((int) queryTimeMs);
    }
}
