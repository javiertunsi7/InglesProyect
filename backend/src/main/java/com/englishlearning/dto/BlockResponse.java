package com.englishlearning.dto;

import java.util.List;

public record BlockResponse(
        Long id,
        Integer position,
        String title,
        String subtitle,
        Integer startExercise,
        Integer endExercise,
        List<ExerciseSummaryResponse> exercises
) {}
