package com.example.demo.application.usecases;

/**
 * Query to retrieve dashboard statistics.
 * The bypassCache flag is for the controller to decide caching strategy;
 * the use case itself does not handle caching.
 */
public record GetDashboardStatsQuery(boolean bypassCache) {
}
