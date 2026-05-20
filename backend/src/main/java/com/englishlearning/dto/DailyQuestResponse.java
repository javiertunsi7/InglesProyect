package com.englishlearning.dto;

public record DailyQuestResponse(
        String type,
        String description,
        int target,
        int progress,
        int rewardXp,
        boolean completed
) {}
