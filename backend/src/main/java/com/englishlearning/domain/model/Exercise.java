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
 * A numbered exercise (1..50) inside a level. Each exercise holds a small set
 * of questions (typically 4-8) that share a topic. Completing the exercise
 * rewards XP and up to 3 stars.
 */
@Entity
@Table(name = "exercises",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_exercises_level_position",
                columnNames = {"level_id", "position"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_id", nullable = false)
    private Long levelId;

    @Column(name = "block_id", nullable = false)
    private Long blockId;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 120)
    private String topic;

    @Column(name = "questions_count", nullable = false)
    private Integer questionsCount;

    @Column(name = "estimated_minutes", nullable = false)
    private Integer estimatedMinutes;

    @Column(name = "xp_reward", nullable = false)
    private Integer xpReward;

    @Column(nullable = false)
    private Boolean locked;
}
