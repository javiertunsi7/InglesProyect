package com.englishlearning.dto;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminDictionaryRequest(
        @NotBlank String word,
        String phonetic,
        String partOfSpeech,
        @NotBlank String definitionEs,
        String exampleEn,
        String exampleEs,
        @NotNull CategoryType categoryType,
        @NotNull LevelCode levelCode
) {}