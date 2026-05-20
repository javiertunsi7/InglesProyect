package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;

import java.util.List;

/**
 * Full dashboard payload (the "Aprender" home page). Built server-side from
 * progress, daily stats, streak and word-of-the-day data so the frontend
 * stays a thin rendering layer.
 */
public record DashboardResponse(
        Greeting greeting,
        ContinueCard continueCard,
        List<CategoryResponse> tracks,
        Streak streak,
        DailyGoal dailyGoal,
        WordOfDayResponse wordOfDay
) {

    public record Greeting(String salutation, String displayName, String headline, String subhead, Integer estimatedMinutes) {}

    public record ContinueCard(
            CategoryType categoryType,
            String categoryName,
            LevelCode levelCode,
            String levelName,
            Long exerciseId,
            Integer exercisePosition,
            String topic,
            Integer questionsRemaining,
            Integer estimatedMinutes,
            /** Preguntas pendientes de repaso hoy (SRS). Null para visitante anónimo. */
            Long dueToday
    ) {}

    public record Streak(
            Integer currentStreak,
            Integer longestStreak,
            Integer totalXp,
            List<StreakDay> week
    ) {}

    public record StreakDay(String dayLabel, boolean active, boolean today) {}

    public record DailyGoal(
            Integer minutesPracticed,
            Integer dailyGoalMinutes,
            Integer xpEarned,
            Integer dailyGoalXp,
            Integer progressPercent,
            Integer minutesRemaining
    ) {}
}
