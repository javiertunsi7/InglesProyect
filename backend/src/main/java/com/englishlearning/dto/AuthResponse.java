package com.englishlearning.dto;

import com.englishlearning.domain.enums.Role;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        Long userId,
        String email,
        String displayName,
        Role role
) {}
