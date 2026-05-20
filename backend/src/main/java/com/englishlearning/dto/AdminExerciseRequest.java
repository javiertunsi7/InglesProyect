package com.englishlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminExerciseRequest(
        @NotNull Long levelId,
        @NotNull Long blockId,
        @NotNull Integer position,
        @NotBlank String title,
        String topic,
        @NotNull Integer questionsCount,
        @NotNull Integer estimatedMinutes,
        @NotNull Integer xpReward,
        Boolean locked
) {}