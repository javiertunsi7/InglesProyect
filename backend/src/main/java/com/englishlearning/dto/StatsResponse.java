package com.englishlearning.dto;

import java.util.List;

public record StatsResponse(
        List<TypeStat> xpByType,
        AccuracyStats todayAccuracy,
        List<DailyBar> weeklyXp,
        List<MonthlyBar> monthlyXp
) {
    public record TypeStat(String type, int xp, int correctCount, int totalCount) {}
    public record AccuracyStats(int correct, int total, double percent) {}
    public record DailyBar(String label, int xp) {}
    public record MonthlyBar(String month, int xp, int exercises) {}
}
