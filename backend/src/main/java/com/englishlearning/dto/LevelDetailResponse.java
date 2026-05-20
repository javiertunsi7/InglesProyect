package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;

import java.util.List;

/**
 * Full payload for the level-detail page: blocks, exercises with status per
 * user, and aggregated stats (completed, next exercise, weekly bars).
 */
public record LevelDetailResponse(
        Long id,
        CategoryType categoryType,
        String categoryName,
        LevelCode code,
        String displayName,
        String headline,
        String description,
        Integer estimatedHours,
        Integer totalExercises,
        Integer completedExercises,
        Integer progressPercent,
        boolean locked,
        List<BlockResponse> blocks,
        ExerciseSummaryResponse nextExercise,
        List<DailyBar> weeklyActivity
) {

    public record DailyBar(String dayLabel, Integer xpEarned, Integer minutesPracticed, boolean active) {}
}
