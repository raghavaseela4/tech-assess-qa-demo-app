package com.example.demo.application.usecases;

import com.example.demo.domain.claim.ClaimRepository;
import com.example.demo.domain.claim.ClaimStatus;
import com.example.demo.domain.dashboard.DashboardStats;
import com.example.demo.user.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for retrieving dashboard statistics.
 * Aggregates user and claim counts for admin dashboard.
 * NO caching logic — pure data aggregation.
 */
@Service
@Transactional(readOnly = true)
public class GetDashboardStatsUseCase {

    private final UserRepository userRepository;
    private final ClaimRepository claimRepository;

    public GetDashboardStatsUseCase(UserRepository userRepository,
                                    ClaimRepository claimRepository) {
        this.userRepository = userRepository;
        this.claimRepository = claimRepository;
    }

    public DashboardStats execute(GetDashboardStatsQuery query) {
        long totalUsers = userRepository.count();
        long totalClaims = claimRepository.count();

        Map<ClaimStatus, Integer> claimsByStatus = claimRepository.countGroupedByStatus()
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().intValue()
                ));

        return new DashboardStats(
            (int) totalUsers,
            (int) totalClaims,
            claimsByStatus
        );
    }
}
