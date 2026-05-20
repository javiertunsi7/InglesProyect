package com.englishlearning.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 120)
        String displayName,

        @Size(max = 500)
        String bio,

        Integer dailyGoalMinutes,

        Integer dailyGoalXp
) {}
