package com.englishlearning.mapper;

import com.englishlearning.domain.model.Level;
import com.englishlearning.dto.LevelSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class LevelMapper {

    public LevelSummaryResponse toSummary(Level level, int completedExercises) {
        int total = level.getTotalExercises();
        int percent = total == 0 ? 0 : (int) Math.round(completedExercises * 100.0 / total);
        return new LevelSummaryResponse(
                level.getId(),
                level.getCode(),
                level.getPosition(),
                level.getDisplayName(),
                level.getDescription(),
                level.getEstimatedHours(),
                total,
                completedExercises,
                Math.min(percent, 100),
                Boolean.TRUE.equals(level.getLocked())
        );
    }
}
