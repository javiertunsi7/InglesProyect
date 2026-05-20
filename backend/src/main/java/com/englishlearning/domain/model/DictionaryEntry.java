package com.englishlearning.domain.model;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Glossary entry shown in the /diccionario page. Each entry belongs to one
 * track (GENERAL/TECH) and an indicative CEFR level so we can scope the
 * vocabulary by what the user is studying.
 */
@Entity
@Table(name = "dictionary_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", nullable = false, length = 32)
    private CategoryType categoryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "level_code", nullable = false, length = 8)
    private LevelCode levelCode;
}
