package com.englishlearning.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalAdminUsers,
        long totalExercises,
        long totalDictionaryEntries
) {}