package com.englishlearning.controller;

import com.englishlearning.dto.DailyQuestResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.service.QuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/quests")
@RequiredArgsConstructor
public class QuestController {

    private final QuestService questService;

    @GetMapping("/today")
    public ResponseEntity<List<DailyQuestResponse>> getTodayQuests(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(questService.getTodayQuests(user.id()));
    }
}
