package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;

import java.util.List;

/**
 * Full payload for the exercise page: meta (title, time, XP), every question
 * and current progress (which questions the user has answered).
 */
public record ExerciseDetailResponse(
        Long id,
        CategoryType categoryType,
        LevelCode levelCode,
        String levelName,
        Integer position,
        String title,
        String topic,
        Integer questionsCount,
        Integer estimatedMinutes,
        Integer xpReward,
        ProgressStatus status,
        Integer stars,
        Integer correctAnswers,
        Integer totalAnswered,
        List<QuestionResponse> questions
) {}
