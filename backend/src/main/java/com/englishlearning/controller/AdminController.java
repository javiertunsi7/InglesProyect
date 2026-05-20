package com.englishlearning.controller;

import com.englishlearning.domain.model.DictionaryEntry;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.WordOfDay;
import com.englishlearning.dto.AdminDictionaryRequest;
import com.englishlearning.dto.AdminExerciseRequest;
import com.englishlearning.dto.AdminStatsResponse;
import com.englishlearning.dto.AdminUserResponse;
import com.englishlearning.dto.AdminWordOfDayRequest;
import com.englishlearning.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/exercises")
    public ResponseEntity<List<Exercise>> listExercises() {
        return ResponseEntity.ok(adminService.listExercises());
    }

    @PostMapping("/exercises")
    public ResponseEntity<Exercise> createExercise(@Valid @RequestBody AdminExerciseRequest request) {
        return ResponseEntity.ok(adminService.createExercise(request));
    }

    @PutMapping("/exercises/{id}")
    public ResponseEntity<Exercise> updateExercise(
            @PathVariable Long id, @Valid @RequestBody AdminExerciseRequest request) {
        return ResponseEntity.ok(adminService.updateExercise(id, request));
    }

    @DeleteMapping("/exercises/{id}")
    public ResponseEntity<Void> deleteExercise(@PathVariable Long id) {
        adminService.deleteExercise(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dictionary")
    public ResponseEntity<List<DictionaryEntry>> listDictionary() {
        return ResponseEntity.ok(adminService.listDictionary());
    }

    @PostMapping("/dictionary")
    public ResponseEntity<DictionaryEntry> createDictionaryEntry(
            @Valid @RequestBody AdminDictionaryRequest request) {
        return ResponseEntity.ok(adminService.createDictionaryEntry(request));
    }

    @PutMapping("/dictionary/{id}")
    public ResponseEntity<DictionaryEntry> updateDictionaryEntry(
            @PathVariable Long id, @Valid @RequestBody AdminDictionaryRequest request) {
        return ResponseEntity.ok(adminService.updateDictionaryEntry(id, request));
    }

    @DeleteMapping("/dictionary/{id}")
    public ResponseEntity<Void> deleteDictionaryEntry(@PathVariable Long id) {
        adminService.deleteDictionaryEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/words")
    public ResponseEntity<List<WordOfDay>> listWordsOfDay() {
        return ResponseEntity.ok(adminService.listWordsOfDay());
    }

    @PostMapping("/words")
    public ResponseEntity<WordOfDay> createWordOfDay(@Valid @RequestBody AdminWordOfDayRequest request) {
        return ResponseEntity.ok(adminService.createWordOfDay(request));
    }

    @PutMapping("/words/{id}")
    public ResponseEntity<WordOfDay> updateWordOfDay(
            @PathVariable Long id, @Valid @RequestBody AdminWordOfDayRequest request) {
        return ResponseEntity.ok(adminService.updateWordOfDay(id, request));
    }

    @DeleteMapping("/words/{id}")
    public ResponseEntity<Void> deleteWordOfDay(@PathVariable Long id) {
        adminService.deleteWordOfDay(id);
        return ResponseEntity.noContent().build();
    }
}