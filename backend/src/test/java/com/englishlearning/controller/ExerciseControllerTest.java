package com.englishlearning.controller;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.enums.QuestionType;
import com.englishlearning.dto.AnswerRequest;
import com.englishlearning.dto.AnswerResponse;
import com.englishlearning.dto.ExerciseDetailResponse;
import com.englishlearning.dto.QuestionResponse;
import com.englishlearning.security.JwtService;
import com.englishlearning.security.SecurityConfig;
import com.englishlearning.service.ExerciseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExerciseController.class)
@Import(SecurityConfig.class)
class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getExercise_shouldReturn200WithQuestions() throws Exception {
        var questions = List.of(
                new QuestionResponse(1L, 1, QuestionType.TRANSLATION, "Hello", null, null, null, null, false, false, null, null, null)
        );
        var response = new ExerciseDetailResponse(1L, CategoryType.GENERAL, LevelCode.A1,
                "Principiante", 1, "Test", "Greetings", 1, 5, 10,
                ProgressStatus.AVAILABLE, 0, 0, 0, questions);
        when(exerciseService.findDetail(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/v1/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test"))
                .andExpect(jsonPath("$.questions.length()").value(1));
    }

    @Test
    void submitAnswer_shouldReturnCorrect() throws Exception {
        var response = new AnswerResponse(true, "¡Correcto!", "hello",
                "Saludo común.", ProgressStatus.IN_PROGRESS, false, 10, 1, 1, 0);
        when(exerciseService.submitAnswer(eq(1L), any(), any())).thenReturn(response);

        var body = new AnswerRequest("hello");
        mockMvc.perform(post("/v1/questions/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.xpEarned").value(10));
    }

    @Test
    void submitAnswer_shouldReturnIncorrect() throws Exception {
        var response = new AnswerResponse(false, "Incorrecto, sigue intentándolo.",
                "hello", null, ProgressStatus.IN_PROGRESS, false, 0, 0, 1, 0);
        when(exerciseService.submitAnswer(eq(1L), any(), any())).thenReturn(response);

        var body = new AnswerRequest("wrong");
        mockMvc.perform(post("/v1/questions/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(false))
                .andExpect(jsonPath("$.xpEarned").value(0));
    }

    @Test
    void submitAnswer_withEmptyBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/v1/questions/1/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
