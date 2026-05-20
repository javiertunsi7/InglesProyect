package com.englishlearning.dto;

import com.englishlearning.domain.enums.LevelCode;

/**
 * A CEFR level summary for the level-selection grid.
 * progressPercent and completedExercises are zero for anonymous users.
 */
public record LevelSummaryResponse(
        Long id,
        LevelCode code,
        Integer position,
        String displayName,
        String description,
        Integer estimatedHours,
        Integer totalExercises,
        Integer completedExercises,
        Integer progressPercent,
        boolean locked
) {}
