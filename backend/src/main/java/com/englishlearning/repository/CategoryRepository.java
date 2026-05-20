package com.englishlearning.repository;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByType(CategoryType type);

    List<Category> findAllByOrderByPositionAsc();
}
