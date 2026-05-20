package com.englishlearning.controller;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.dto.LevelDetailResponse;
import com.englishlearning.dto.LevelSummaryResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/categories/{categoryType}/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;

    @GetMapping
    public ResponseEntity<List<LevelSummaryResponse>> getLevels(
            @PathVariable CategoryType categoryType,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        return ResponseEntity.ok(levelService.findByCategoryType(categoryType, userId));
    }

    @GetMapping("/{code}")
    public ResponseEntity<LevelDetailResponse> getLevelDetail(
            @PathVariable CategoryType categoryType,
            @PathVariable LevelCode code,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        return ResponseEntity.ok(levelService.findDetail(categoryType, code, userId));
    }
}
