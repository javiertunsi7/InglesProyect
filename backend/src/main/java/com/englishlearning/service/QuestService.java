package com.englishlearning.service;

import com.englishlearning.domain.enums.QuestType;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.dto.DailyQuestResponse;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import com.englishlearning.repository.UserStreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestService {

    private final UserStreakRepository streakRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserQuestionAttemptRepository attemptRepository;

    public List<DailyQuestResponse> getTodayQuests(Long userId) {
        LocalDate today = LocalDate.now();
        UserStreak streak = streakRepository.findByUserId(userId).orElse(null);
        int totalXp = streak != null ? streak.getTotalXp() : 0;

        // Determinar dificultad según XP total
        int tier = totalXp < 1000 ? 0 : totalXp < 5000 ? 1 : 2;

        // Generar semilla determinista para hoy
        long seed = (userId * 31L + today.toEpochDay()) * 7L;
        Random rng = new Random(seed);

        // Seleccionar 3 tipos únicos
        QuestType[] allTypes = QuestType.values();
        List<QuestType> selected = new ArrayList<>();
        while (selected.size() < 3) {
            QuestType t = allTypes[rng.nextInt(allTypes.length)];
            if (!selected.contains(t)) selected.add(t);
        }

        // Datos de progreso de hoy
        UserDailyStats stats = dailyStatsRepository.findByUserIdAndOnDate(userId, today).orElse(null);
        int todayCompleted = stats != null ? stats.getExercisesCompleted() : 0;
        int todayXp = stats != null ? stats.getXpEarned() : 0;
        int todayReviews = countTodayReviews(userId, today);
        int currentStreak = streak != null ? streak.getCurrentStreak() : 0;
        int todayPerfect = countTodayPerfect(userId, today);

        List<DailyQuestResponse> quests = new ArrayList<>();
        for (QuestType type : selected) {
            quests.add(buildQuest(type, tier, todayCompleted, todayXp, todayReviews, currentStreak, todayPerfect));
        }
        return quests;
    }

    private DailyQuestResponse buildQuest(QuestType type, int tier,
                                          int todayCompleted, int todayXp,
                                          int todayReviews, int currentStreak,
                                          int todayPerfect) {
        return switch (type) {
            case COMPLETE_EXERCISES -> {
                int target = tier == 0 ? 3 : tier == 1 ? 5 : 8;
                yield new DailyQuestResponse(
                        "COMPLETE_EXERCISES",
                        "Completa " + target + " ejercicios",
                        target, Math.min(todayCompleted, target),
                        tier == 0 ? 50 : tier == 1 ? 100 : 150,
                        todayCompleted >= target);
            }
            case EARN_XP -> {
                int target = tier == 0 ? 100 : tier == 1 ? 200 : 300;
                yield new DailyQuestResponse(
                        "EARN_XP",
                        "Gana " + target + " XP",
                        target, Math.min(todayXp, target),
                        tier == 0 ? 50 : tier == 1 ? 100 : 150,
                        todayXp >= target);
            }
            case COMPLETE_REVIEWS -> {
                int target = tier == 0 ? 3 : tier == 1 ? 5 : 8;
                yield new DailyQuestResponse(
                        "COMPLETE_REVIEWS",
                        "Completa " + target + " repasos SRS",
                        target, Math.min(todayReviews, target),
                        tier == 0 ? 50 : tier == 1 ? 100 : 150,
                        todayReviews >= target);
            }
            case STREAK_MAINTENANCE -> {
                int target = tier == 0 ? 1 : tier == 1 ? 3 : 5;
                yield new DailyQuestResponse(
                        "STREAK_MAINTENANCE",
                        "Mantén una racha de " + target + " días",
                        target, Math.min(currentStreak, target),
                        tier == 0 ? 50 : tier == 1 ? 100 : 150,
                        currentStreak >= target);
            }
            case PERFECT_SCORE -> {
                int target = tier == 0 ? 1 : tier == 1 ? 2 : 3;
                yield new DailyQuestResponse(
                        "PERFECT_SCORE",
                        "Consigue " + target + " ejercicios con 3 estrellas",
                        target, Math.min(todayPerfect, target),
                        tier == 0 ? 50 : tier == 1 ? 100 : 150,
                        todayPerfect >= target);
            }
        };
    }

    private int countTodayReviews(Long userId, LocalDate today) {
        ZoneId zone = ZoneId.systemDefault();
        List<UserQuestionAttempt> all = attemptRepository.findByUserId(userId);
        return (int) all.stream()
                .filter(a -> a.getLastSeenAt() != null
                        && a.getLastSeenAt().atZone(zone).toLocalDate().equals(today))
                .count();
    }

    private int countTodayPerfect(Long userId, LocalDate today) {
        ZoneId zone = ZoneId.systemDefault();
        List<UserExerciseProgress> all = progressRepository.findByUserIdOrderByLastSeenAtDesc(userId);
        return (int) all.stream()
                .filter(p -> p.getStatus() == com.englishlearning.domain.enums.ProgressStatus.COMPLETED
                        && p.getStars() == 3
                        && p.getLastSeenAt() != null
                        && p.getLastSeenAt().atZone(zone).toLocalDate().equals(today))
                .count();
    }
}
