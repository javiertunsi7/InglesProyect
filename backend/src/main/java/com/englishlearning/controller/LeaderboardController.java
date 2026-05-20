package com.englishlearning.controller;

import com.englishlearning.dto.LeaderboardResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public ResponseEntity<LeaderboardResponse> getLeaderboard(
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        return ResponseEntity.ok(leaderboardService.getLeaderboard(userId));
    }
}