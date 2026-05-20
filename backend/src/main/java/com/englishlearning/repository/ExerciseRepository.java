package com.englishlearning.repository;

import com.englishlearning.domain.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findByLevelIdOrderByPositionAsc(Long levelId);

    List<Exercise> findByBlockIdOrderByPositionAsc(Long blockId);

    Optional<Exercise> findByLevelIdAndPosition(Long levelId, Integer position);
}
