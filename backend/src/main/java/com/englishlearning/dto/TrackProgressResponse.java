package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;

import java.util.List;

/**
 * Progreso del usuario en una pista (GENERAL/TECH) con desglose por nivel CEFR.
 */
public record TrackProgressResponse(
        CategoryType categoryType,
        String displayName,
        Integer totalExercises,
        Integer completedExercises,
        Integer progressPercent,
        Integer xpEarned,
        List<LevelSummaryResponse> levels
) {}
