package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.dto.CategoryResponse;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.mapper.CategoryMapper;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll(Long userId) {
        return categoryRepository.findAllByOrderByPositionAsc().stream()
                .map(category -> toResponse(category, userId))
                .toList();
    }

    public CategoryResponse findByType(CategoryType type, Long userId) {
        Category category = categoryRepository.findByType(type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la categoría con tipo " + type));
        return toResponse(category, userId);
    }

    private CategoryResponse toResponse(Category category, Long userId) {
        List<Level> levels = levelRepository.findByCategoryIdOrderByPositionAsc(category.getId());
        int totalLevels = levels.size();
        if (userId == null) {
            return categoryMapper.toResponse(category, totalLevels, 0);
        }
        List<Long> levelIds = levels.stream().map(Level::getId).toList();
        int total = 0;
        int completed = 0;
        for (Long levelId : levelIds) {
            List<Exercise> exercises = exerciseRepository.findByLevelIdOrderByPositionAsc(levelId);
            total += exercises.size();
            if (exercises.isEmpty()) continue;
            List<Long> ids = exercises.stream().map(Exercise::getId).toList();
            Set<Long> completedIds = new HashSet<>();
            for (UserExerciseProgress p : progressRepository.findByUserIdAndExerciseIdIn(userId, ids)) {
                if (p.getStatus() == ProgressStatus.COMPLETED) {
                    completedIds.add(p.getExerciseId());
                }
            }
            completed += completedIds.size();
        }
        int percent = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        return categoryMapper.toResponse(category, totalLevels, Math.min(percent, 100));
    }
}
