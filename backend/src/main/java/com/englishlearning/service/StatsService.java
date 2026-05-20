package com.englishlearning.service;

import com.englishlearning.domain.model.Question;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.dto.StatsResponse;
import com.englishlearning.repository.QuestionRepository;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserQuestionAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    public StatsResponse getStats(Long userId) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();

        // ── XP por tipo de pregunta ──────────────────────────────────────
        List<UserQuestionAttempt> allAttempts = attemptRepository.findByUserId(userId);
        List<Long> questionIds = allAttempts.stream()
                .map(UserQuestionAttempt::getQuestionId)
                .distinct()
                .toList();
        Map<Long, Question> questionsById = questionRepository.findByIdInOrderByPositionAsc(questionIds)
                .stream().collect(Collectors.toMap(Question::getId, q -> q));

        Map<String, int[]> byType = new HashMap<>(); // type → [xp, correct, total]
        for (UserQuestionAttempt a : allAttempts) {
            Question q = questionsById.get(a.getQuestionId());
            if (q == null) continue;
            String type = q.getType().name();
            int[] counts = byType.computeIfAbsent(type, k -> new int[3]);
            counts[2]++; // total
            if (Boolean.TRUE.equals(a.getCorrect())) {
                counts[0] += 10; // xp
                counts[1]++; // correct
            }
        }
        List<StatsResponse.TypeStat> xpByType = byType.entrySet().stream()
                .map(e -> new StatsResponse.TypeStat(e.getKey(), e.getValue()[0],
                        e.getValue()[1], e.getValue()[2]))
                .toList();

        // ── Precisión de hoy ─────────────────────────────────────────────
        List<UserQuestionAttempt> allAttemptsList = attemptRepository.findByUserId(userId);
        int todayCorrect = 0;
        int todayTotal = 0;
        for (UserQuestionAttempt a : allAttemptsList) {
            if (a.getLastSeenAt() != null && a.getLastSeenAt().atZone(zone).toLocalDate().equals(today)) {
                todayTotal++;
                if (Boolean.TRUE.equals(a.getCorrect())) todayCorrect++;
            }
        }
        StatsResponse.AccuracyStats accuracy = new StatsResponse.AccuracyStats(
                todayCorrect, todayTotal,
                todayTotal == 0 ? 0 : Math.round(todayCorrect * 100.0 / todayTotal));

        // ── XP semanal ──────────────────────────────────────────────────
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<UserDailyStats> weekStats = dailyStatsRepository
                .findByUserIdAndOnDateBetweenOrderByOnDateAsc(userId, weekStart, today);
        Map<LocalDate, UserDailyStats> byWeekDate = new HashMap<>();
        for (UserDailyStats s : weekStats) byWeekDate.put(s.getOnDate(), s);

        String[] dayLabels = {"L", "M", "M", "J", "V", "S", "D"};
        List<StatsResponse.DailyBar> weeklyXp = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            UserDailyStats s = byWeekDate.get(day);
            weeklyXp.add(new StatsResponse.DailyBar(dayLabels[i], s != null ? s.getXpEarned() : 0));
        }

        // ── XP mensual (últimos 12 meses) ───────────────────────────────
        LocalDate yearAgo = today.minusMonths(11).withDayOfMonth(1);
        List<UserDailyStats> yearStats = dailyStatsRepository
                .findByUserIdAndOnDateBetweenOrderByOnDateAsc(userId, yearAgo, today);

        Map<String, int[]> byMonth = new HashMap<>(); // "Ene 2025" → [xp, exercises]
        for (UserDailyStats s : yearStats) {
            String monthKey = s.getOnDate().getMonth().getDisplayName(
                    TextStyle.SHORT, new Locale("es", "ES")) + " "
                    + s.getOnDate().getYear();
            int[] vals = byMonth.computeIfAbsent(monthKey, k -> new int[2]);
            vals[0] += s.getXpEarned();
            vals[1] += s.getExercisesCompleted();
        }
        List<StatsResponse.MonthlyBar> monthlyXp = byMonth.entrySet().stream()
                .map(e -> new StatsResponse.MonthlyBar(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .toList();

        return new StatsResponse(xpByType, accuracy, weeklyXp, monthlyXp);
    }
}
