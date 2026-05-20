package com.englishlearning.dto;

import com.englishlearning.domain.enums.Role;

public record UserBadgeResponse(
        Long id,
        String email,
        String displayName,
        String initials,
        Integer currentStreak,
        Integer totalXp,
        Role role
) {}
