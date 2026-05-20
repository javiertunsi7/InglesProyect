package com.englishlearning.controller;

import com.englishlearning.dto.AchievementResponse;
import com.englishlearning.dto.ProgressOverviewResponse;
import com.englishlearning.dto.TrackProgressResponse;
import com.englishlearning.service.ProgressOverviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressOverviewService overviewService;

    @Test
    @WithMockUser
    void getProgress_shouldReturn200WithOverview() throws Exception {
        var response = new ProgressOverviewResponse(2480, 14, 21,
                120, 23, 56, 2,
                List.of(), List.of(), List.of(), List.of());
        when(overviewService.build(any())).thenReturn(response);

        mockMvc.perform(get("/v1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalXp").value(2480))
                .andExpect(jsonPath("$.currentStreak").value(14))
                .andExpect(jsonPath("$.lifetimeMinutes").value(120));
    }

    @Test
    void getProgress_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/v1/progress"))
                .andExpect(status().isUnauthorized());
    }
}
