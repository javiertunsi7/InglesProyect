package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Block;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.dto.BlockResponse;
import com.englishlearning.dto.ExerciseSummaryResponse;
import com.englishlearning.dto.LevelDetailResponse;
import com.englishlearning.dto.LevelSummaryResponse;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.mapper.ExerciseMapper;
import com.englishlearning.mapper.LevelMapper;
import com.englishlearning.repository.BlockRepository;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LevelService {

    private static final String[] DAY_LABELS = {"L", "M", "M", "J", "V", "S", "D"};

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final BlockRepository blockRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserDailyStatsRepository dailyStatsRepository;
    private final LevelMapper levelMapper;
    private final ExerciseMapper exerciseMapper;
    private final SubscriptionService subscriptionService;

    public List<LevelSummaryResponse> findByCategoryType(CategoryType type, Long userId) {
        Category category = resolveCategory(type);
        boolean premium = userId != null && subscriptionService.isPremium(userId);
        return levelRepository.findByCategoryIdOrderByPositionAsc(category.getId()).stream()
                .map(level -> levelMapper.toSummary(level, completedCount(level, userId)))
                .map(summary -> applyPremiumLock(summary, premium))
                .toList();
    }

    public LevelDetailResponse findDetail(CategoryType type, LevelCode code, Long userId) {
        Category category = resolveCategory(type);
        boolean premium = userId != null && subscriptionService.isPremium(userId);
        Level level = levelRepository.findByCategoryIdAndCode(category.getId(), code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el nivel " + code + " en la categoría " + type));
        List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(level.getId());
        Map<Long, UserExerciseProgress> progressByExercise = loadProgress(exercises, userId);
        List<ExerciseSummaryResponse> exerciseSummaries = exercises.stream()
                .map(ex -> exerciseMapper.toSummary(ex, progressByExercise.get(ex.getId())))
                .toList();

        List<Block> blocks = blockRepository.findByLevelIdOrderByPositionAsc(level.getId());
        Map<Long, List<ExerciseSummaryResponse>> exercisesByBlock = new HashMap<>();
        for (Exercise exercise : exercises) {
            exercisesByBlock
                    .computeIfAbsent(exercise.getBlockId(), key -> new ArrayList<>())
                    .add(exerciseMapper.toSummary(exercise, progressByExercise.get(exercise.getId())));
        }
        for (List<ExerciseSummaryResponse> list : exercisesByBlock.values()) {
            list.sort(Comparator.comparingInt(ExerciseSummaryResponse::position));
        }
        List<BlockResponse> blockResponses = blocks.stream()
                .map(b -> new BlockResponse(
                        b.getId(),
                        b.getPosition(),
                        b.getTitle(),
                        b.getSubtitle(),
                        b.getStartExercise(),
                        b.getEndExercise(),
                        exercisesByBlock.getOrDefault(b.getId(), List.of())))
                .toList();

        int completed = (int) exerciseSummaries.stream()
                .filter(e -> e.status() == ProgressStatus.COMPLETED)
                .count();
        int total = exercises.size();
        int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);

        ExerciseSummaryResponse next = exerciseSummaries.stream()
                .filter(e -> e.status() != ProgressStatus.COMPLETED && e.status() != ProgressStatus.LOCKED)
                .findFirst()
                .orElse(exerciseSummaries.isEmpty() ? null : exerciseSummaries.get(0));

        boolean locked = Boolean.TRUE.equals(level.getLocked());
        if (!premium && (code == LevelCode.C1 || code == LevelCode.C2)) {
            locked = true;
        }

        return new LevelDetailResponse(
                level.getId(),
                type,
                category.getDisplayName(),
                code,
                level.getDisplayName(),
                level.getHeadline(),
                level.getDescription(),
                level.getEstimatedHours(),
                total,
                completed,
                Math.min(percent, 100),
                locked,
                blockResponses,
                next,
                weeklyActivity(userId)
        );
    }

    private int completedCount(Level level, Long userId) {
        if (userId == null) return 0;
        List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(level.getId());
        if (exercises.isEmpty()) return 0;
        List<Long> ids = exercises.stream().map(Exercise::getId).toList();
        int count = 0;
        for (UserExerciseProgress p : progressRepository.findByUserIdAndExerciseIdIn(userId, ids)) {
            if (p.getStatus() == ProgressStatus.COMPLETED) count++;
        }
        return count;
    }

    private Map<Long, UserExerciseProgress> loadProgress(List<Exercise> exercises, Long userId) {
        if (userId == null || exercises.isEmpty()) return Map.of();
        List<Long> ids = exercises.stream().map(Exercise::getId).toList();
        Map<Long, UserExerciseProgress> byExercise = new HashMap<>();
        for (UserExerciseProgress p : progressRepository.findByUserIdAndExerciseIdIn(userId, ids)) {
            byExercise.put(p.getExerciseId(), p);
        }
        return byExercise;
    }

    private List<LevelDetailResponse.DailyBar> weeklyActivity(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        List<LevelDetailResponse.DailyBar> bars = new ArrayList<>();
        Map<LocalDate, UserDailyStats> byDate = new HashMap<>();
        if (userId != null) {
            for (UserDailyStats s : dailyStatsRepository
                    .findByUserIdAndOnDateBetweenOrderByOnDateAsc(userId, weekStart, weekStart.plusDays(6))) {
                byDate.put(s.getOnDate(), s);
            }
        }
        for (int i = 0; i < 7; i++) {
            LocalDate day = weekStart.plusDays(i);
            UserDailyStats s = byDate.get(day);
            int xp = s == null ? 0 : s.getXpEarned();
            int min = s == null ? 0 : s.getMinutesPracticed();
            bars.add(new LevelDetailResponse.DailyBar(
                    DAY_LABELS[i],
                    xp,
                    min,
                    !day.isAfter(today) && (s != null && s.getXpEarned() > 0)
            ));
        }
        return bars;
    }

    private LevelSummaryResponse applyPremiumLock(LevelSummaryResponse s, boolean premium) {
        if (!premium && (s.code() == LevelCode.C1 || s.code() == LevelCode.C2)) {
            return new LevelSummaryResponse(
                    s.id(), s.code(), s.position(),
                    s.displayName(), s.description(),
                    s.estimatedHours(), s.totalExercises(),
                    s.completedExercises(), s.progressPercent(),
                    true);
        }
        return s;
    }

    private Category resolveCategory(CategoryType type) {
        return categoryRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la categoría con tipo " + type));
    }
}
