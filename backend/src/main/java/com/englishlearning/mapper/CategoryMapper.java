package com.englishlearning.mapper;

import com.englishlearning.domain.model.Category;
import com.englishlearning.dto.CategoryResponse;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category, int totalLevels, int progressPercent) {
        return new CategoryResponse(
                category.getId(),
                category.getType(),
                category.getDisplayName(),
                category.getTagline(),
                category.getDescription(),
                category.getPosition(),
                totalLevels,
                progressPercent
        );
    }
}
