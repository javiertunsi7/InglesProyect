package com.englishlearning.dto;

public record LeaderboardEntry(
        int rank,
        Long userId,
        String displayName,
        String initials,
        Integer totalXp,
        Integer currentStreak,
        Integer longestStreak
) {}