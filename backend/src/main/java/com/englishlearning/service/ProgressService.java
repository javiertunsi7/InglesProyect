package com.englishlearning.service;

import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import com.englishlearning.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Central place where XP, stars, exercise status, streak and daily stats are
 * updated. Every answer flows through {@link #recordQuestionAttempt(long, Exercise, com.englishlearning.domain.model.Question, boolean)}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService {

    private static final int XP_PER_CORRECT_QUESTION = 10;
    private static final int DEFAULT_DAILY_GOAL_MINUTES = 10;
    private static final int DEFAULT_DAILY_GOAL_XP = 100;

    private final UserExerciseProgressRepository exerciseProgressRepository;
    private final UserQuestionAttemptRepository questionAttemptRepository;
    private final UserStreakRepository streakRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final SrsService srsService;

    /**
     * Records an attempt on a question and updates the parent exercise's
     * progress in one transaction. Returns the updated exercise progress.
     */
    public AttemptOutcome recordQuestionAttempt(long userId,
                                                Exercise exercise,
                                                com.englishlearning.domain.model.Question question,
                                                boolean correct) {
        UserQuestionAttempt attempt = questionAttemptRepository
                .findByUserIdAndQuestionId(userId, question.getId())
                .orElseGet(() -> UserQuestionAttempt.builder()
                        .userId(userId)
                        .questionId(question.getId())
                        .attempts(0)
                        .correct(false)
                        .hintsUsed(0)
                        .lastSeenAt(Instant.now())
                        .repetitions(0)
                        .easeFactor(SrsService.INITIAL_EASE)
                        .intervalDays(0)
                        .nextReviewDate(srsService.today())
                        .build());

        boolean firstSolve = correct && !Boolean.TRUE.equals(attempt.getCorrect());
        attempt.setAttempts(attempt.getAttempts() + 1);
        attempt.setCorrect(correct || Boolean.TRUE.equals(attempt.getCorrect()));
        attempt.setLastSeenAt(Instant.now());
        // SRS: programa la próxima vez que esta pregunta debe aparecer.
        srsService.scheduleNext(attempt, correct);
        questionAttemptRepository.save(attempt);

        UserExerciseProgress progress = exerciseProgressRepository
                .findByUserIdAndExerciseId(userId, exercise.getId())
                .orElseGet(() -> UserExerciseProgress.builder()
                        .userId(userId)
                        .exerciseId(exercise.getId())
                        .status(ProgressStatus.AVAILABLE)
                        .stars(0)
                        .correctAnswers(0)
                        .totalAnswers(0)
                        .questionsDone(0)
                        .xpEarned(0)
                        .lastSeenAt(Instant.now())
                        .build());

        progress.setTotalAnswers(progress.getTotalAnswers() + 1);
        if (correct) {
            progress.setCorrectAnswers(progress.getCorrectAnswers() + 1);
        }
        if (firstSolve) {
            progress.setQuestionsDone(progress.getQuestionsDone() + 1);
        }

        boolean justCompleted = false;
        int xpAwarded = 0;
        if (firstSolve) {
            xpAwarded = XP_PER_CORRECT_QUESTION;
            progress.setXpEarned(progress.getXpEarned() + xpAwarded);
        }

        int totalQ = exercise.getQuestionsCount();
        if (progress.getQuestionsDone() >= totalQ) {
            if (progress.getStatus() != ProgressStatus.COMPLETED) {
                progress.setStatus(ProgressStatus.COMPLETED);
                progress.setCompletedAt(Instant.now());
                progress.setStars(computeStars(progress, totalQ));
                xpAwarded += exercise.getXpReward();
                progress.setXpEarned(progress.getXpEarned() + exercise.getXpReward());
                justCompleted = true;
            }
        } else if (progress.getStatus() == ProgressStatus.AVAILABLE) {
            progress.setStatus(ProgressStatus.IN_PROGRESS);
        }
        progress.setLastSeenAt(Instant.now());
        UserExerciseProgress saved = exerciseProgressRepository.save(progress);

        if (xpAwarded > 0) {
            updateStreakAndStats(userId, xpAwarded, justCompleted, exercise.getEstimatedMinutes());
        }
        return new AttemptOutcome(saved, justCompleted, xpAwarded);
    }

    /**
     * Initializes (idempotently) a user's streak row and seeds the very first
     * day with a small XP bonus so newly created accounts look alive.
     */
    public UserStreak ensureStreak(Long userId) {
        return streakRepository.findByUserId(userId)
                .orElseGet(() -> streakRepository.save(UserStreak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalXp(0)
                        .build()));
    }

    public UserDailyStats ensureTodayStats(Long userId, LocalDate today) {
        return dailyStatsRepository.findByUserIdAndOnDate(userId, today)
                .orElseGet(() -> dailyStatsRepository.save(UserDailyStats.builder()
                        .userId(userId)
                        .onDate(today)
                        .xpEarned(0)
                        .minutesPracticed(0)
                        .exercisesCompleted(0)
                        .dailyGoalMinutes(DEFAULT_DAILY_GOAL_MINUTES)
                        .dailyGoalXp(DEFAULT_DAILY_GOAL_XP)
                        .build()));
    }

    private void updateStreakAndStats(long userId, int xp, boolean justCompletedExercise, int minutesSpent) {
        LocalDate today = LocalDate.now();
        UserDailyStats stats = ensureTodayStats(userId, today);
        stats.setXpEarned(stats.getXpEarned() + xp);
        stats.setMinutesPracticed(stats.getMinutesPracticed() + Math.max(1, minutesSpent / 4));
        if (justCompletedExercise) {
            stats.setExercisesCompleted(stats.getExercisesCompleted() + 1);
        }
        dailyStatsRepository.save(stats);

        UserStreak streak = ensureStreak(userId);
        streak.setTotalXp(streak.getTotalXp() + xp);
        LocalDate last = streak.getLastActiveDate();
        if (last == null || ChronoUnit.DAYS.between(last, today) >= 2) {
            streak.setCurrentStreak(1);
        } else if (last.isBefore(today)) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        }
        streak.setLongestStreak(Math.max(streak.getLongestStreak(), streak.getCurrentStreak()));
        streak.setLastActiveDate(today);
        streakRepository.save(streak);
    }

    private int computeStars(UserExerciseProgress progress, int totalQuestions) {
        if (totalQuestions == 0) return 0;
        double accuracy = progress.getCorrectAnswers() * 1.0 / progress.getTotalAnswers();
        if (accuracy >= 0.95) return 3;
        if (accuracy >= 0.75) return 2;
        return 1;
    }

    public record AttemptOutcome(UserExerciseProgress progress, boolean justCompleted, int xpAwarded) {}
}
