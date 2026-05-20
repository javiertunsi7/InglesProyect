package com.englishlearning.mapper;

import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.dto.ExerciseSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public ExerciseSummaryResponse toSummary(Exercise exercise, UserExerciseProgress progress) {
        ProgressStatus status = resolveStatus(exercise, progress);
        Integer stars = progress != null ? progress.getStars() : 0;
        return new ExerciseSummaryResponse(
                exercise.getId(),
                exercise.getPosition(),
                exercise.getTitle(),
                exercise.getTopic(),
                exercise.getQuestionsCount(),
                exercise.getEstimatedMinutes(),
                exercise.getXpReward(),
                status,
                stars
        );
    }

    private ProgressStatus resolveStatus(Exercise exercise, UserExerciseProgress progress) {
        if (progress != null) {
            return progress.getStatus();
        }
        return Boolean.TRUE.equals(exercise.getLocked())
                ? ProgressStatus.LOCKED
                : ProgressStatus.AVAILABLE;
    }
}
