package com.englishlearning.repository;

import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {

    List<Level> findByCategoryIdOrderByPositionAsc(Long categoryId);

    Optional<Level> findByCategoryIdAndCode(Long categoryId, LevelCode code);
}
