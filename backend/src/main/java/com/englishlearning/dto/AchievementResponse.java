package com.englishlearning.dto;

/**
 * Logro / hito del usuario, derivado en memoria a partir de los stats.
 */
public record AchievementResponse(
        String code,
        String title,
        String description,
        String icon,
        boolean unlocked,
        Integer progress,
        Integer target
) {}
