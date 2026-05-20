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

import java.time.LocalDate;

@Entity
@Table(name = "words_of_day")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordOfDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "on_date", nullable = false, unique = true)
    private LocalDate onDate;

    @Column(nullable = false, length = 80)
    private String word;

    @Column(length = 80)
    private String phonetic;

    @Column(name = "part_of_speech", length = 40)
    private String partOfSpeech;

    @Column(name = "definition_es", nullable = false, length = 255)
    private String definitionEs;

    @Column(name = "example_en", length = 255)
    private String exampleEn;

    @Column(name = "example_es", length = 255)
    private String exampleEs;
}
