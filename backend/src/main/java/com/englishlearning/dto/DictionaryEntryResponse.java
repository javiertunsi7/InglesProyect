package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;

/**
 * Entrada del diccionario expuesta al frontend en /v1/dictionary.
 */
public record DictionaryEntryResponse(
        Long id,
        String word,
        String phonetic,
        String partOfSpeech,
        String definitionEs,
        String exampleEn,
        String exampleEs,
        CategoryType categoryType,
        LevelCode levelCode
) {}
