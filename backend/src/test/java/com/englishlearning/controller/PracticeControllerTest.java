package com.englishlearning.controller;

import com.englishlearning.domain.enums.QuestionType;
import com.englishlearning.domain.enums.Role;
import com.englishlearning.dto.DailyPracticeResponse;
import com.englishlearning.dto.QuestionResponse;
import com.englishlearning.security.AuthenticatedUser;
import com.englishlearning.security.JwtService;
import com.englishlearning.security.SecurityConfig;
import com.englishlearning.service.PracticeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PracticeController.class)
@Import(SecurityConfig.class)
class PracticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PracticeService practiceService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getDaily_shouldReturn200WithSession() throws Exception {
        var questions = List.of(
                new QuestionResponse(1L, 1, QuestionType.TRANSLATION, "Hello", null, null, null, null, false, false, null, null, null)
        );
        var forecast = List.of(
                new DailyPracticeResponse.ForecastDay(LocalDate.now(), 5L)
        );
        var session = new DailyPracticeResponse(LocalDate.now(), "Práctica diaria",
                "Repasa y aprende", 10, 50, 3, 2, forecast, questions);
        when(practiceService.buildDailySession(any())).thenReturn(session);

        mockMvc.perform(get("/v1/practice/daily")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headline").value("Práctica diaria"))
                .andExpect(jsonPath("$.dueCount").value(3))
                .andExpect(jsonPath("$.newCount").value(2))
                .andExpect(jsonPath("$.questions.length()").value(1));
    }

    @Test
    void getDaily_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/v1/practice/daily"))
                .andExpect(status().isUnauthorized());
    }

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        var principal = new AuthenticatedUser(42L, "demo@english.local", "Demo User", Role.USER);
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
