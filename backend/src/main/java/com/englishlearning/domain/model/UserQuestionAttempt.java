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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "user_question_attempts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_question_attempts",
                columnNames = {"user_id", "question_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Integer attempts;

    @Column(nullable = false)
    private Boolean correct;

    @Column(name = "hints_used", nullable = false)
    private Integer hintsUsed;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    /* === Spaced Repetition System (SM-2 simplificado) ===
     * Cada attempt lleva su propio scheduler. La sesión diaria se construye
     * pidiendo las preguntas con next_review_date <= hoy (ver SrsService).
     */

    @Column(nullable = false)
    private Integer repetitions;

    @Column(name = "ease_factor", nullable = false, precision = 3, scale = 2)
    private BigDecimal easeFactor;

    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @Column(name = "next_review_date", nullable = false)
    private LocalDate nextReviewDate;
}
