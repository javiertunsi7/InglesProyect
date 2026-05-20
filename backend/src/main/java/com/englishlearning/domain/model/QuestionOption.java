package com.englishlearning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A selectable option for a MULTIPLE_CHOICE question. Each option has a
 * letter-style label (A, B, C, ...) and the underlying value.
 */
@Entity
@Table(name = "question_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, length = 255)
    private String label;

    @Column(name = "option_value", nullable = false, length = 255)
    private String value;

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;

    @Column(name = "match_group", length = 32)
    private String matchGroup;
}
