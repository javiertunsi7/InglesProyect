package com.englishlearning.repository;

import com.englishlearning.domain.model.UserExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserExerciseProgressRepository extends JpaRepository<UserExerciseProgress, Long> {

    Optional<UserExerciseProgress> findByUserIdAndExerciseId(Long userId, Long exerciseId);

    List<UserExerciseProgress> findByUserIdAndExerciseIdIn(Long userId, Collection<Long> exerciseIds);

    List<UserExerciseProgress> findByUserIdOrderByLastSeenAtDesc(Long userId);
}
