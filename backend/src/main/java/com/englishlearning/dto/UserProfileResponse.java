package com.englishlearning.dto;

import com.englishlearning.domain.enums.Role;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String displayName,
        String initials,
        String bio,
        String avatarUrl,
        Role role,
        Integer currentStreak,
        Integer totalXp,
        Integer dailyGoalMinutes,
        Integer dailyGoalXp,
        Instant createdAt
) {}
