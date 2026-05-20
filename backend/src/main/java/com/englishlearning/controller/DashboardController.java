package com.englishlearning.controller;

import com.englishlearning.dto.DashboardResponse;
import com.englishlearning.dto.UserBadgeResponse;
import com.englishlearning.dto.WordOfDayResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(dashboardService.buildDashboard(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserBadgeResponse> getCurrentUser(@AuthenticationPrincipal AuthenticatedUser user) {
        UserBadgeResponse response = dashboardService.buildUserBadge(user);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @GetMapping("/words/today")
    public ResponseEntity<WordOfDayResponse> getWordOfDay() {
        WordOfDayResponse response = dashboardService.wordOfDayResponse();
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }
}
