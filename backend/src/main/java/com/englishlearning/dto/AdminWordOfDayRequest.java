package com.englishlearning.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record AdminWordOfDayRequest(
        @NotBlank LocalDate onDate,
        @NotBlank String word,
        String phonetic,
        String partOfSpeech,
        @NotBlank String definitionEs,
        String exampleEn,
        String exampleEs
) {}