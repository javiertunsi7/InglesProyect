package com.englishlearning.controller;

import com.englishlearning.dto.AnswerRequest;
import com.englishlearning.dto.AnswerResponse;
import com.englishlearning.dto.ExerciseDetailResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/exercises/{exerciseId}")
    public ResponseEntity<ExerciseDetailResponse> getExercise(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(exerciseService.findDetail(exerciseId, user));
    }

    @PostMapping("/questions/{questionId}/answer")
    public ResponseEntity<AnswerResponse> submitAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(exerciseService.submitAnswer(questionId, request, user));
    }
}
