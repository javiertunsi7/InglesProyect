package com.englishlearning.dto;

import java.time.LocalDate;

public record WordOfDayResponse(
        LocalDate date,
        String word,
        String phonetic,
        String partOfSpeech,
        String definitionEs,
        String exampleEn,
        String exampleEs
) {}
