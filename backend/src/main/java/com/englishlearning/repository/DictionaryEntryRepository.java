package com.englishlearning.repository;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.model.DictionaryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, Long> {

    /**
     * Full search with optional filters. Any {@code null} parameter is treated
     * as "no filter" thanks to the SpEL-like null check in the JPQL.
     */
    @Query("""
           SELECT d FROM DictionaryEntry d
            WHERE (:q IS NULL OR LOWER(d.word) LIKE LOWER(CONCAT('%', :q, '%'))
                                  OR LOWER(d.definitionEs) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:category IS NULL OR d.categoryType = :category)
              AND (:level    IS NULL OR d.levelCode    = :level)
            ORDER BY d.word ASC
           """)
    Page<DictionaryEntry> search(
            @Param("q") String q,
            @Param("category") CategoryType category,
            @Param("level") LevelCode level,
            Pageable pageable);
}
