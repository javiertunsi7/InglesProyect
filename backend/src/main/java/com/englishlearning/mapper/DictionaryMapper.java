package com.englishlearning.mapper;

import com.englishlearning.domain.model.DictionaryEntry;
import com.englishlearning.dto.DictionaryEntryResponse;
import org.springframework.stereotype.Component;

@Component
public class DictionaryMapper {

    public DictionaryEntryResponse toResponse(DictionaryEntry entry) {
        return new DictionaryEntryResponse(
                entry.getId(),
                entry.getWord(),
                entry.getPhonetic(),
                entry.getPartOfSpeech(),
                entry.getDefinitionEs(),
                entry.getExampleEn(),
                entry.getExampleEs(),
                entry.getCategoryType(),
                entry.getLevelCode()
        );
    }
}
