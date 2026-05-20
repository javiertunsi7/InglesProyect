package com.englishlearning.dto;

import com.englishlearning.domain.enums.Role;
import java.time.Instant;

public record AdminUserResponse(
        Long id,
        String email,
        String displayName,
        Role role,
        Instant createdAt,
        Integer totalXp,
        Integer currentStreak
) {}