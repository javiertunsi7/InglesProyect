package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.Question;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.dto.AchievementResponse;
import com.englishlearning.dto.LevelDetailResponse;
import com.englishlearning.dto.LevelSummaryResponse;
import com.englishlearning.dto.ProgressOverviewResponse;
import com.englishlearning.dto.TrackProgressResponse;
import com.englishlearning.mapper.LevelMapper;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.QuestionRepository;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserStreakRepository;
import com.englishlearning.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressOverviewService {

    private static final int HISTORY_DAYS = 14;

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final UserStreakRepository streakRepository;
    private final UserQuestionAttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final LevelMapper levelMapper;

    public ProgressOverviewResponse build(AuthenticatedUser user) {
        Long userId = user.id();

        List<UserExerciseProgress> allProgress = progressRepository.findByUserIdOrderByLastSeenAtDesc(userId);
        Map<Long, UserExerciseProgress> progressByExercise = new HashMap<>();
        for (UserExerciseProgress p : allProgress) {
            progressByExercise.put(p.getExerciseId(), p);
        }

        int totalCompleted = (int) allProgress.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();
        int totalStars = allProgress.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .mapToInt(UserExerciseProgress::getStars)
                .sum();
        int averageStars = totalCompleted == 0
                ? 0
                : (int) Math.round(totalStars * 10.0 / totalCompleted);

        List<TrackProgressResponse> tracks = buildTracks(userId, progressByExercise);
        List<LevelDetailResponse.DailyBar> history = buildHistory(userId);
        int lifetimeMinutes = history.stream().mapToInt(LevelDetailResponse.DailyBar::minutesPracticed).sum();
        UserStreak streak = streakRepository.findByUserId(userId).orElse(null);
        int totalXp = streak != null ? streak.getTotalXp() : 0;
        int currentStreak = streak != null ? streak.getCurrentStreak() : 0;
        int longestStreak = streak != null ? streak.getLongestStreak() : 0;

        List<AchievementResponse> achievements = buildAchievements(userId);

        List<ProgressOverviewResponse.SrsForecastDay> srsForecast = buildSrsForecast(userId);

        return new ProgressOverviewResponse(
                totalXp,
                currentStreak,
                longestStreak,
                lifetimeMinutes,
                totalCompleted,
                totalStars,
                averageStars,
                tracks,
                history,
                srsForecast,
                achievements
        );
    }

    private List<ProgressOverviewResponse.SrsForecastDay> buildSrsForecast(Long userId) {
        List<ProgressOverviewResponse.SrsForecastDay> forecast = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            LocalDate day = today.plusDays(i);
            long count = attemptRepository.countByUserIdAndNextReviewDateLessThanEqual(userId, day);
            forecast.add(new ProgressOverviewResponse.SrsForecastDay(day, count));
        }
        return forecast;
    }

    private List<TrackProgressResponse> buildTracks(Long userId, Map<Long, UserExerciseProgress> progressByExercise) {
        List<TrackProgressResponse> out = new ArrayList<>();
        for (Category category : categoryRepository.findAllByOrderByPositionAsc()) {
            List<Level> levels = levelRepository.findByCategoryIdOrderByPositionAsc(category.getId());
            List<LevelSummaryResponse> levelSummaries = new ArrayList<>();
            int trackCompleted = 0;
            int trackTotal = 0;
            int trackXp = 0;
            for (Level level : levels) {
                List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(level.getId());
                int levelCompleted = 0;
                for (Exercise ex : exercises) {
                    UserExerciseProgress p = progressByExercise.get(ex.getId());
                    if (p != null) {
                        if (p.getStatus() == ProgressStatus.COMPLETED) levelCompleted++;
                        trackXp += p.getXpEarned();
                    }
                }
                trackCompleted += levelCompleted;
                trackTotal += exercises.size();
                levelSummaries.add(levelMapper.toSummary(level, levelCompleted));
            }
            int trackPercent = trackTotal == 0 ? 0 : (int) Math.round(trackCompleted * 100.0 / trackTotal);
            out.add(new TrackProgressResponse(
                    category.getType(),
                    category.getDisplayName(),
                    trackTotal,
                    trackCompleted,
                    trackPercent,
                    trackXp,
                    levelSummaries
            ));
        }
        return out;
    }

    private List<LevelDetailResponse.DailyBar> buildHistory(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(HISTORY_DAYS - 1L);
        Map<LocalDate, UserDailyStats> byDate = new HashMap<>();
        for (UserDailyStats s : dailyStatsRepository
                .findByUserIdAndOnDateBetweenOrderByOnDateAsc(userId, start, today)) {
            byDate.put(s.getOnDate(), s);
        }
        List<LevelDetailResponse.DailyBar> bars = new ArrayList<>(HISTORY_DAYS);
        for (int i = 0; i < HISTORY_DAYS; i++) {
            LocalDate day = start.plusDays(i);
            UserDailyStats s = byDate.get(day);
            int xp = s == null ? 0 : s.getXpEarned();
            int min = s == null ? 0 : s.getMinutesPracticed();
            String label = day.getDayOfWeek().getDisplayName(TextStyle.NARROW, new Locale("es", "ES")).toUpperCase();
            boolean active = xp > 0;
            bars.add(new LevelDetailResponse.DailyBar(label, xp, min, active));
        }
        return bars;
    }

    private List<AchievementResponse> buildAchievements(Long userId) {
        List<AchievementResponse> list = new ArrayList<>();

        // ── Data ──────────────────────────────────────────────────────────
        User user = userRepository.findById(userId).orElse(null);
        UserStreak streak = streakRepository.findByUserId(userId).orElse(null);
        List<UserExerciseProgress> allProgress = progressRepository.findByUserIdOrderByLastSeenAtDesc(userId);

        int totalCompleted = (int) allProgress.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .count();
        int totalStars = allProgress.stream()
                .filter(p -> p.getStatus() == ProgressStatus.COMPLETED)
                .mapToInt(UserExerciseProgress::getStars)
                .sum();
        int totalXp = streak != null ? streak.getTotalXp() : 0;
        int currentStreak = streak != null ? streak.getCurrentStreak() : 0;
        int longestStreak = streak != null ? streak.getLongestStreak() : 0;

        // ── Tracks (para logros de niveles) ────────────────────────────────
        Map<Long, UserExerciseProgress> progressByExercise = new HashMap<>();
        for (UserExerciseProgress p : allProgress) {
            progressByExercise.put(p.getExerciseId(), p);
        }
        List<TrackProgressResponse> tracks = buildTracks(userId, progressByExercise);

        // ── Attempts ───────────────────────────────────────────────────────
        List<UserQuestionAttempt> allAttempts = attemptRepository.findByUserId(userId);
        long totalReviews = allAttempts.stream().mapToLong(UserQuestionAttempt::getAttempts).sum();
        long masteredCount = allAttempts.stream()
                .filter(a -> a.getEaseFactor() != null
                        && a.getEaseFactor().compareTo(BigDecimal.valueOf(2.50)) >= 0)
                .count();

        // ── Daily stats ──────────────────────────────────────────────────
        LocalDate today = LocalDate.now();
        LocalDate yearAgo = today.minusDays(364);
        List<UserDailyStats> allStats = dailyStatsRepository
                .findByUserIdAndOnDateBetweenOrderByOnDateAsc(userId, yearAgo, today);
        Map<LocalDate, UserDailyStats> statsByDate = new HashMap<>();
        for (UserDailyStats s : allStats) {
            statsByDate.put(s.getOnDate(), s);
        }

        // ═══════════════════════════════════════════════════════════════════
        //  GRUPO 1 — Series
        // ═══════════════════════════════════════════════════════════════════
        list.add(ach("fifty_exercises", "Cincuenta ejercicios",
                "Completa 50 ejercicios.", "🏅", totalCompleted >= 50, totalCompleted, 50));
        list.add(ach("xp_25000", "Veinticinco mil XP",
                "Acumula 25.000 XP totales.", "⚡⚡⚡", totalXp >= 25000, totalXp, 25000));
        list.add(ach("streak_100", "Racha de 100 días",
                "Mantén la racha 100 días seguidos.", "🔥🔥🔥", longestStreak >= 100, longestStreak, 100));
        list.add(ach("streak_365", "Racha de 365 días",
                "Un año entero sin perder el ritmo.", "👑", longestStreak >= 365, longestStreak, 365));
        list.add(ach("stars_100", "Cien estrellas",
                "Acumula 100 estrellas en total.", "✨✨", totalStars >= 100, totalStars, 100));
        boolean hasPerfect = hasPerfectLevel(allProgress, progressByExercise);
        list.add(ach("perfect_level", "Nivel perfecto",
                "Consigue 3 estrellas en todos los ejercicios de un nivel.", "🎓🎓",
                hasPerfect, hasPerfect ? 1 : 0, 1));

        // ═══════════════════════════════════════════════════════════════════
        //  GRUPO 2 — Exploración
        // ═══════════════════════════════════════════════════════════════════
        boolean explored = hasExploredAllTypes(allAttempts);
        list.add(ach("explorer", "Explorador",
                "Responde correctamente un ejercicio de cada tipo.", "🧭",
                explored, explored ? 1 : 0, 1));
        boolean allComplete = tracks.stream()
                .allMatch(t -> t.totalExercises() > 0 && t.completedExercises() >= t.totalExercises());
        list.add(ach("polyglot", "Políglota",
                "Completa todas las pistas al 100%.", "🌎",
                allComplete, allComplete ? 1 : 0, 1));
        boolean c2Done = hasCompletedC2(tracks);
        list.add(ach("master_c2", "Maestro",
                "Completa C2 en cualquier pista.", "🏆",
                c2Done, c2Done ? 1 : 0, 1));
        boolean allVisited = tracks.stream().allMatch(t -> t.completedExercises() > 0);
        list.add(ach("traveler", "Viajero",
                "Ten progreso en todas las pistas.", "🗺️",
                allVisited, allVisited ? 1 : 0, 1));

        // ═══════════════════════════════════════════════════════════════════
        //  GRUPO 3 — Consistencia
        // ═══════════════════════════════════════════════════════════════════
        int consecutiveGoalDays = countConsecutiveGoalDays(statsByDate, today);
        list.add(ach("week_complete", "Semana completa",
                "Cumple tu meta diaria 7 días seguidos.", "📅",
                consecutiveGoalDays >= 7, Math.min(consecutiveGoalDays, 7), 7));
        list.add(ach("month_complete", "Mes completo",
                "Cumple tu meta diaria 30 días seguidos.", "📅📅",
                consecutiveGoalDays >= 30, Math.min(consecutiveGoalDays, 30), 30));

        int activeWeeks = countActiveWeeks(statsByDate, today);
        list.add(ach("constant", "Constante",
                "5 semanas con al menos 3 días activos.", "🔄",
                activeWeeks >= 5, Math.min(activeWeeks, 5), 5));

        ZoneId zone = ZoneId.systemDefault();
        boolean hasNightOwl = allProgress.stream()
                .filter(p -> p.getLastSeenAt() != null)
                .anyMatch(p -> p.getLastSeenAt().atZone(zone).toLocalTime().getHour() >= 20);
        list.add(ach("night_owl", "Noctámbulo",
                "Practica después de las 20:00.", "🦉", hasNightOwl, hasNightOwl ? 1 : 0, 1));

        long overdueCount = attemptRepository.countByUserIdAndNextReviewDateLessThanEqual(userId, today);
        boolean noOverdue = overdueCount == 0;
        list.add(ach("early_bird", "Madrugador",
                "No tengas repasos atrasados.", "☕", noOverdue,
                noOverdue ? 1 : 0, 1));

        // ═══════════════════════════════════════════════════════════════════
        //  GRUPO 4 — Calidad SRS
        // ═══════════════════════════════════════════════════════════════════
        list.add(ach("master_10", "Dominio",
                "Domina 10 preguntas (ease factor ≥ 2.50).", "🧠",
                masteredCount >= 10, (int) Math.min(masteredCount, 10), 10));
        list.add(ach("master_50", "Sabio",
                "Domina 50 preguntas (ease factor ≥ 2.50).", "🧠🧠",
                masteredCount >= 50, (int) Math.min(masteredCount, 50), 50));
        list.add(ach("reviewer_100", "Repasador",
                "Completa 100 repasos totales.", "📚",
                totalReviews >= 100, (int) Math.min(totalReviews, 100), 100));

        // ═══════════════════════════════════════════════════════════════════
        //  GRUPO 5 — Social / Cuenta
        // ═══════════════════════════════════════════════════════════════════
        boolean hasProfile = user != null
                && (user.getBio() != null && !user.getBio().isBlank()
                || user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank());
        list.add(ach("profile_set", "Meta personal",
                "Establece tu biografía o avatar.", "💪",
                hasProfile, hasProfile ? 1 : 0, 1));

        boolean isAnniversary = user != null && user.getCreatedAt() != null
                && user.getCreatedAt().isBefore(Instant.now().minus(Duration.ofDays(365)));
        list.add(ach("anniversary", "Aniversario",
                "Llevas un año con nosotros.", "🎂",
                isAnniversary, isAnniversary ? 1 : 0, 1));

        // ═══════════════════════════════════════════════════════════════════
        //  Logros originales (se mantienen)
        // ═══════════════════════════════════════════════════════════════════
        list.add(ach("first_exercise", "Primer ejercicio", "Completa tu primer ejercicio.",
                "⭐", totalCompleted >= 1, totalCompleted, 1));
        list.add(ach("ten_exercises", "Diez ejercicios", "Completa 10 ejercicios.",
                "🏅", totalCompleted >= 10, totalCompleted, 10));
        list.add(ach("streak_7", "Racha de 7 días", "Mantén la llama encendida una semana.",
                "🔥", longestStreak >= 7, longestStreak, 7));
        list.add(ach("streak_30", "Racha de 30 días", "Un mes seguido practicando.",
                "🔥🔥", longestStreak >= 30, longestStreak, 30));
        list.add(ach("xp_1000", "Mil XP", "Acumula 1.000 XP totales.",
                "⚡", totalXp >= 1000, totalXp, 1000));
        list.add(ach("xp_5000", "Cinco mil XP", "Acumula 5.000 XP totales.",
                "⚡⚡", totalXp >= 5000, totalXp, 5000));
        list.add(ach("perfectionist", "Perfeccionista", "Consigue 25 estrellas en total.",
                "✨", totalStars >= 25, totalStars, 25));
        boolean firstLevel = tracks.stream()
                .flatMap(t -> t.levels().stream())
                .filter(Objects::nonNull)
                .anyMatch(l -> l.totalExercises() != null
                        && l.totalExercises() > 0
                        && l.completedExercises().equals(l.totalExercises()));
        list.add(ach("first_level", "Primer nivel completo", "Termina los 50 ejercicios de un nivel.",
                "🎓", firstLevel, firstLevel ? 1 : 0, 1));

        return list;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private boolean hasPerfectLevel(List<UserExerciseProgress> allProgress,
                                    Map<Long, UserExerciseProgress> progressByExercise) {
        Map<Long, List<Exercise>> exercisesByLevel = new HashMap<>();
        for (UserExerciseProgress p : allProgress) {
            if (p.getStatus() != ProgressStatus.COMPLETED) continue;
            Exercise ex = exerciseRepository.findById(p.getExerciseId()).orElse(null);
            if (ex == null) continue;
            exercisesByLevel.computeIfAbsent(ex.getLevelId(), k -> new ArrayList<>()).add(ex);
        }
        for (Map.Entry<Long, List<Exercise>> entry : exercisesByLevel.entrySet()) {
            List<Exercise> levelExercises = exerciseRepository
                    .findByLevelIdOrderByPositionAsc(entry.getKey());
            if (levelExercises.isEmpty()) continue;
            boolean allPerfect = levelExercises.stream().allMatch(ex -> {
                UserExerciseProgress pr = progressByExercise.get(ex.getId());
                return pr != null && pr.getStatus() == ProgressStatus.COMPLETED && pr.getStars() == 3;
            });
            if (allPerfect) return true;
        }
        return false;
    }

    private boolean hasExploredAllTypes(List<UserQuestionAttempt> attempts) {
        if (attempts.isEmpty()) return false;
        Set<Long> correctQuestionIds = attempts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getCorrect()))
                .map(UserQuestionAttempt::getQuestionId)
                .collect(Collectors.toSet());
        if (correctQuestionIds.isEmpty()) return false;
        List<Question> questions = questionRepository.findByIdInOrderByPositionAsc(
                new ArrayList<>(correctQuestionIds));
        Set<String> typesDone = questions.stream()
                .map(q -> q.getType().name())
                .collect(Collectors.toSet());
        return typesDone.containsAll(Set.of(
                "TRANSLATION", "REVERSE_TRANSLATION", "MULTIPLE_CHOICE",
                "FILL_BLANK", "LISTENING", "WORD_ORDER", "MATCHING", "DICTATION"));
    }

    private boolean hasCompletedC2(List<TrackProgressResponse> tracks) {
        for (TrackProgressResponse track : tracks) {
            for (LevelSummaryResponse level : track.levels()) {
                if (level.code() == LevelCode.C2
                        && level.completedExercises() != null
                        && level.totalExercises() != null
                        && level.totalExercises() > 0
                        && level.completedExercises().equals(level.totalExercises())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int countConsecutiveGoalDays(Map<LocalDate, UserDailyStats> statsByDate, LocalDate today) {
        int count = 0;
        for (int i = 0; i < 365; i++) {
            LocalDate day = today.minusDays(i);
            UserDailyStats s = statsByDate.get(day);
            if (s != null && s.getXpEarned() >= s.getDailyGoalXp()) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private int countActiveWeeks(Map<LocalDate, UserDailyStats> statsByDate, LocalDate today) {
        int weeks = 0;
        for (int w = 0; w < 5; w++) {
            LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1)
                    .minusWeeks(w);
            int activeDays = 0;
            for (int d = 0; d < 7; d++) {
                LocalDate day = weekStart.plusDays(d);
                UserDailyStats s = statsByDate.get(day);
                if (s != null && s.getXpEarned() > 0) {
                    activeDays++;
                }
            }
            if (activeDays >= 3) weeks++;
        }
        return weeks;
    }

    private AchievementResponse ach(String code, String title, String desc,
                                    String icon, boolean unlocked,
                                    int progress, int target) {
        return new AchievementResponse(code, title, desc, icon, unlocked,
                Math.min(progress, target), target);
    }
}
