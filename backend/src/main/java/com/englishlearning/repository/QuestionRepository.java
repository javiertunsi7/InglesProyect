package com.englishlearning.repository;

import com.englishlearning.domain.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByExerciseIdOrderByPositionAsc(Long exerciseId);

    List<Question> findByExerciseIdInOrderByExerciseIdAscPositionAsc(java.util.Collection<Long> exerciseIds);

    List<Question> findByIdInOrderByPositionAsc(java.util.Collection<Long> ids);
}
