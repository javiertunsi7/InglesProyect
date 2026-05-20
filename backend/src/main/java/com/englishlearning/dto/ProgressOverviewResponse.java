package com.englishlearning.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resumen global de progreso del usuario que se muestra en /progreso.
 * Agrega métricas lifetime (XP, minutos, ejercicios, estrellas), desglose por
 * pista, historial de 14 días, forecast SRS y logros derivados.
 */
public record ProgressOverviewResponse(
        Integer totalXp,
        Integer currentStreak,
        Integer longestStreak,
        Integer lifetimeMinutes,
        Integer totalCompletedExercises,
        Integer totalStars,
        Integer averageStars,
        List<TrackProgressResponse> tracks,
        List<LevelDetailResponse.DailyBar> history,
        /** Cuántos repasos te toca hacer cada día durante los próximos 14 días. */
        List<SrsForecastDay> srsForecast,
        List<AchievementResponse> achievements
) {

    public record SrsForecastDay(LocalDate date, Long count) {}
}
