package com.englishlearning.dto;

import java.util.List;

public record LeaderboardResponse(
        List<LeaderboardEntry> entries,
        LeaderboardEntry currentUser
) {}