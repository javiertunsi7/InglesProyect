package com.englishlearning.dto;

import com.englishlearning.domain.enums.ProgressStatus;

/**
 * Numbered exercise card on the level-detail page. {@code stars} ranges 0..3
 * and is filled with the user's best result; anonymous users always see 0.
 */
public record ExerciseSummaryResponse(
        Long id,
        Integer position,
        String title,
        String topic,
        Integer questionsCount,
        Integer estimatedMinutes,
        Integer xpReward,
        ProgressStatus status,
        Integer stars
) {}
