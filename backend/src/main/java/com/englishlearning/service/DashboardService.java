package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.domain.model.WordOfDay;
import com.englishlearning.dto.CategoryResponse;
import com.englishlearning.dto.DashboardResponse;
import com.englishlearning.dto.UserBadgeResponse;
import com.englishlearning.dto.WordOfDayResponse;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserStreakRepository;
import com.englishlearning.repository.WordOfDayRepository;
import com.englishlearning.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final String[] DAY_LABELS = {"L", "M", "M", "J", "V", "S", "D"};

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserStreakRepository streakRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final WordOfDayRepository wordOfDayRepository;
    private final UserQuestionAttemptRepository attemptRepository;

    public DashboardResponse buildDashboard(AuthenticatedUser principal) {
        Long userId = principal != null ? principal.id() : null;
        String displayName = principal != null ? principal.displayName() : "invitado";

        List<CategoryResponse> tracks = categoryService.findAll(userId);
        DashboardResponse.Greeting greeting = buildGreeting(displayName, tracks);
        // Para el visitante anónimo enseñamos un continueCard genérico que apunta
        // al primer ejercicio, pero ocultamos racha y meta diaria — esas piezas
        // solo cobran sentido cuando hay sesión.
        DashboardResponse.ContinueCard continueCard = buildContinueCard(userId);
        DashboardResponse.Streak streak = userId != null ? buildStreak(userId) : null;
        DashboardResponse.DailyGoal dailyGoal = userId != null ? buildDailyGoal(userId) : null;
        WordOfDayResponse word = wordOfDayResponse();
        return new DashboardResponse(greeting, continueCard, tracks, streak, dailyGoal, word);
    }

    public UserBadgeResponse buildUserBadge(AuthenticatedUser principal) {
        if (principal == null) {
            return null;
        }
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario con id " + principal.id()));
        Optional<UserStreak> streak = streakRepository.findByUserId(user.getId());
        return new UserBadgeResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                initials(user.getDisplayName()),
                streak.map(UserStreak::getCurrentStreak).orElse(0),
                streak.map(UserStreak::getTotalXp).orElse(0),
                user.getRole()
        );
    }

    public WordOfDayResponse wordOfDayResponse() {
        WordOfDay word = wordOfDayRepository.findByOnDate(LocalDate.now())
                .or(wordOfDayRepository::findFirstByOrderByOnDateDesc)
                .orElse(null);
        if (word == null) return null;
        return new WordOfDayResponse(
                word.getOnDate(),
                word.getWord(),
                word.getPhonetic(),
                word.getPartOfSpeech(),
                word.getDefinitionEs(),
                word.getExampleEn(),
                word.getExampleEs()
        );
    }

    private DashboardResponse.Greeting buildGreeting(String displayName, List<CategoryResponse> tracks) {
        int hour = LocalDateTime.now().getHour();
        String salutation;
        if (hour < 12) salutation = "Buenos días";
        else if (hour < 20) salutation = "Buenas tardes";
        else salutation = "Buenas noches";
        String headline = "Hoy practicas conversación.";
        if (tracks.size() > 1) {
            CategoryResponse tech = tracks.stream()
                    .filter(t -> t.type() == CategoryType.TECH)
                    .findFirst()
                    .orElse(tracks.get(0));
            headline = "Hoy practicas " + (tech.tagline() != null ? tech.tagline().toLowerCase() : "inglés técnico") + ".";
        }
        String subhead = "Continúa donde lo dejaste, o explora una pista nueva. Cada sesión te toma 8 minutos.";
        return new DashboardResponse.Greeting(salutation, displayName, headline, subhead, 8);
    }

    private DashboardResponse.ContinueCard buildContinueCard(Long userId) {
        if (userId == null) {
            return defaultContinueCard();
        }
        List<UserExerciseProgress> recent = progressRepository.findByUserIdOrderByLastSeenAtDesc(userId);
        for (UserExerciseProgress progress : recent) {
            if (progress.getStatus() == ProgressStatus.COMPLETED) continue;
            Optional<Exercise> ex = exerciseRepository.findById(progress.getExerciseId());
            if (ex.isEmpty()) continue;
            Exercise exercise = ex.get();
            Optional<Level> level = levelRepository.findById(exercise.getLevelId());
            if (level.isEmpty()) continue;
            Optional<Category> category = categoryRepository.findById(level.get().getCategoryId());
            if (category.isEmpty()) continue;
            int remaining = Math.max(exercise.getQuestionsCount() - progress.getQuestionsDone(), 1);
            long dueToday = attemptRepository.countByUserIdAndNextReviewDateLessThanEqual(userId, LocalDate.now());
            return new DashboardResponse.ContinueCard(
                    category.get().getType(),
                    category.get().getDisplayName(),
                    level.get().getCode(),
                    level.get().getDisplayName(),
                    exercise.getId(),
                    exercise.getPosition(),
                    exercise.getTopic() != null ? exercise.getTopic() : exercise.getTitle(),
                    remaining,
                    Math.max(exercise.getEstimatedMinutes() - 2, 2),
                    dueToday
            );
        }
        return defaultContinueCard();
    }

    private DashboardResponse.ContinueCard defaultContinueCard() {
        Optional<Category> techCategory = categoryRepository.findByType(CategoryType.TECH);
        if (techCategory.isEmpty()) return null;
        List<Level> levels = levelRepository.findByCategoryIdOrderByPositionAsc(techCategory.get().getId());
        if (levels.isEmpty()) return null;
        Level level = levels.get(0);
        List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(level.getId());
        if (exercises.isEmpty()) return null;
        Exercise exercise = exercises.get(0);
        return new DashboardResponse.ContinueCard(
                techCategory.get().getType(),
                techCategory.get().getDisplayName(),
                level.getCode(),
                level.getDisplayName(),
                exercise.getId(),
                exercise.getPosition(),
                exercise.getTopic() != null ? exercise.getTopic() : exercise.getTitle(),
                exercise.getQuestionsCount(),
                exercise.getEstimatedMinutes(),
                null
        );
    }

    private DashboardResponse.Streak buildStreak(Long userId) {
        UserStreak streak = userId == null
                ? null
                : streakRepository.findByUserId(userId).orElse(null);
        int current = streak == null ? 0 : streak.getCurrentStreak();
        int longest = streak == null ? 0 : streak.getLongestStreak();
        int totalXp = streak == null ? 0 : streak.getTotalXp();
        return new DashboardResponse.Streak(current, longest, totalXp, weekDays(userId));
    }

    private List<DashboardResponse.StreakDay> weekDays(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<DashboardResponse.StreakDay> days = new ArrayList<>();
        List<UserDailyStats> stats = userId == null
                ? List.of()
                : dailyStatsRepository.findByUserIdAndOnDateBetweenOrderByOnDateAsc(
                        userId, weekStart, weekStart.plusDays(6));
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            boolean active = stats.stream()
                    .anyMatch(s -> s.getOnDate().equals(day) && s.getXpEarned() > 0);
            days.add(new DashboardResponse.StreakDay(DAY_LABELS[i], active, day.equals(today)));
        }
        return days;
    }

    private DashboardResponse.DailyGoal buildDailyGoal(Long userId) {
        LocalDate today = LocalDate.now();
        Optional<UserDailyStats> statsOpt = userId == null
                ? Optional.empty()
                : dailyStatsRepository.findByUserIdAndOnDate(userId, today);
        int minutes = statsOpt.map(UserDailyStats::getMinutesPracticed).orElse(0);
        int xp = statsOpt.map(UserDailyStats::getXpEarned).orElse(0);
        int goalMinutes = statsOpt.map(UserDailyStats::getDailyGoalMinutes).orElse(10);
        int goalXp = statsOpt.map(UserDailyStats::getDailyGoalXp).orElse(100);
        int percent = goalMinutes == 0 ? 0 : (int) Math.round(minutes * 100.0 / goalMinutes);
        int remaining = Math.max(goalMinutes - minutes, 0);
        return new DashboardResponse.DailyGoal(
                minutes,
                goalMinutes,
                xp,
                goalXp,
                Math.min(percent, 100),
                remaining
        );
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }
}
