package com.englishlearning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Thematic block inside a level. Groups consecutive exercises by topic
 * (e.g. "Fundamentos del software" covers exercises 1-10 inside the A2 level).
 */
@Entity
@Table(name = "blocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_blocks_level_position",
                columnNames = {"level_id", "position"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 180)
    private String subtitle;

    @Column(name = "start_exercise", nullable = false)
    private Integer startExercise;

    @Column(name = "end_exercise", nullable = false)
    private Integer endExercise;
}
