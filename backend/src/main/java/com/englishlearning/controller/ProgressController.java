package com.englishlearning.controller;

import com.englishlearning.dto.ProgressOverviewResponse;
import com.englishlearning.dto.StatsResponse;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.ProgressOverviewService;
import com.englishlearning.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressOverviewService overviewService;
    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<ProgressOverviewResponse> get(
            @AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new BadRequestException("Inicia sesión para ver tu progreso.");
        }
        return ResponseEntity.ok(overviewService.build(user));
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(statsService.getStats(user.id()));
    }
}
