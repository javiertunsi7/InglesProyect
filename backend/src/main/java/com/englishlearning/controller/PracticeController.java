package com.englishlearning.controller;

import com.englishlearning.dto.DailyPracticeResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    @GetMapping("/daily")
    public ResponseEntity<DailyPracticeResponse> getDaily(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(practiceService.buildDailySession(user));
    }
}
