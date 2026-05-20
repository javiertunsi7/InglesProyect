package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.dto.DictionaryEntryResponse;
import com.englishlearning.dto.DictionaryPageResponse;
import com.englishlearning.mapper.DictionaryMapper;
import com.englishlearning.repository.DictionaryEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Búsqueda paginada del diccionario. La búsqueda es case-insensitive y aplica
 * sobre {@code word} y {@code definition_es}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DictionaryService {

    private static final int DEFAULT_PAGE_SIZE = 24;
    private static final int MAX_PAGE_SIZE = 60;

    private final DictionaryEntryRepository repository;
    private final DictionaryMapper mapper;

    public DictionaryPageResponse search(String query,
                                         CategoryType category,
                                         LevelCode level,
                                         int page,
                                         int size) {
        String normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
        int safePage = Math.max(0, page);
        int safeSize = clamp(size <= 0 ? DEFAULT_PAGE_SIZE : size, 1, MAX_PAGE_SIZE);

        Page<DictionaryEntryResponse> result = repository
                .search(normalizedQuery, category, level, PageRequest.of(safePage, safeSize))
                .map(mapper::toResponse);

        List<DictionaryEntryResponse> items = result.getContent();
        return new DictionaryPageResponse(
                items,
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages()
        );
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
