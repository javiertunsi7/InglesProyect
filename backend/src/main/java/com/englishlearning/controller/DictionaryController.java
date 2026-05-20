package com.englishlearning.controller;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.dto.DictionaryPageResponse;
import com.englishlearning.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dictionary")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping
    public ResponseEntity<DictionaryPageResponse> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "category", required = false) CategoryType category,
            @RequestParam(value = "level", required = false) LevelCode level,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "24") int size) {
        return ResponseEntity.ok(
                dictionaryService.search(query, category, level, page, size));
    }
}
