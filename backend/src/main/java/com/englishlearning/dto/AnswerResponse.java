package com.englishlearning.dto;

import com.englishlearning.domain.enums.ProgressStatus;

/**
 * Result of submitting an answer to a question.
 * - correct          : true if the answer matched.
 * - message          : Spanish, user-friendly feedback.
 * - correctAnswer    : revealed only after the user answers.
 * - explanation      : Spanish explanation shown after answering.
 * - exerciseStatus   : updated lifecycle stage for the parent exercise.
 * - exerciseCompleted: convenience flag for the UI to celebrate finishing.
 * - xpEarned         : XP awarded in this attempt (zero for retries).
 */
public record AnswerResponse(
        boolean correct,
        String message,
        String correctAnswer,
        String explanation,
        ProgressStatus exerciseStatus,
        boolean exerciseCompleted,
        Integer xpEarned,
        Integer correctAnswers,
        Integer totalAnswered,
        Integer stars
) {}
