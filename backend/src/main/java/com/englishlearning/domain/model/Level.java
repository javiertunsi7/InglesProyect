package com.englishlearning.domain.model;

import com.englishlearning.domain.enums.LevelCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One CEFR level (A1..C2) inside a category. Each level groups 5 thematic
 * blocks of 10 exercises (50 exercises total). C1 and C2 are typically locked
 * until earlier levels are completed.
 */
@Entity
@Table(name = "levels",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_levels_category_code",
                columnNames = {"category_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Level {

    public static final int EXERCISES_PER_LEVEL = 50;
    public static final int BLOCKS_PER_LEVEL = 5;
    public static final int EXERCISES_PER_BLOCK = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private LevelCode code;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(length = 180)
    private String headline;

    @Column(length = 500)
    private String description;

    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours;

    @Column(name = "total_exercises", nullable = false)
    private Integer totalExercises;

    @Column(nullable = false)
    private Boolean locked;
}
