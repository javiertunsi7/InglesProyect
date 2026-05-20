package com.englishlearning.domain.enums;

/**
 * Lifecycle of a user's progress on a specific exercise.
 * LOCKED      -> the user cannot access it yet (previous exercise pending).
 * AVAILABLE   -> can be started but has not been attempted.
 * IN_PROGRESS -> the user has answered at least one question.
 * COMPLETED   -> every question of the exercise has been answered correctly.
 */
public enum ProgressStatus {
    LOCKED, AVAILABLE, IN_PROGRESS, COMPLETED
}
