package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;

/**
 * Lightweight category list item. Stats (total levels and progress percentage)
 * come from the service layer aggregating exercises and user progress.
 */
public record CategoryResponse(
        Long id,
        CategoryType type,
        String displayName,
        String tagline,
        String description,
        Integer position,
        Integer totalLevels,
        Integer progressPercent
) {}
