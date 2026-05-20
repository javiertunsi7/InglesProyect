package com.englishlearning.controller;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.dto.CategoryResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        return ResponseEntity.ok(categoryService.findAll(userId));
    }

    @GetMapping("/{categoryType}")
    public ResponseEntity<CategoryResponse> getCategory(
            @PathVariable CategoryType categoryType,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        return ResponseEntity.ok(categoryService.findByType(categoryType, userId));
    }
}
