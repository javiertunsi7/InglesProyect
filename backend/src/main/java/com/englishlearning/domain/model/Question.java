package com.englishlearning.domain.model;

import com.englishlearning.domain.enums.QuestionType;
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
 * A single question inside an exercise. Carries the prompt, optional context,
 * an optional hint, the correct answer and the explanation shown after the
 * user replies.
 *
 * promptHighlight is the substring of the prompt rendered in italic accent
 * color (e.g. the word 'issue' in "¿Qué significa 'issue' en español?").
 */
@Entity
@Table(name = "questions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_questions_exercise_position",
                columnNames = {"exercise_id", "position"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private QuestionType type;

    @Column(nullable = false, length = 500)
    private String prompt;

    @Column(name = "prompt_highlight", length = 120)
    private String promptHighlight;

    @Column(length = 255)
    private String context;

    @Column(length = 255)
    private String hint;

    @Column(name = "correct_answer", nullable = false, length = 255)
    private String correctAnswer;

    @Column(name = "audio_text", length = 255)
    private String audioText;

    @Column(length = 800)
    private String explanation;
}
