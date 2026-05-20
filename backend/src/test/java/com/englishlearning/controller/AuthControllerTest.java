package com.englishlearning.controller;

import com.englishlearning.dto.AuthResponse;
import com.englishlearning.dto.LoginRequest;
import com.englishlearning.dto.RegisterRequest;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_shouldReturn200WithToken() throws Exception {
        var response = new AuthResponse("jwt-token", 7200L, 1L, "a@b.c", "Test", com.englishlearning.domain.enums.Role.USER);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        var body = new RegisterRequest("a@b.c", "pass1234", "Test");
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("a@b.c"));
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        var body = new RegisterRequest("not-an-email", "pass1234", "Test");
        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn200WithToken() throws Exception {
        var response = new AuthResponse("jwt-token", 7200L, 1L, "a@b.c", "Test", com.englishlearning.domain.enums.Role.USER);
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        var body = new LoginRequest("a@b.c", "pass1234");
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_withWrongCredentials_shouldReturn400() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Credenciales inválidas."));

        var body = new LoginRequest("a@b.c", "wrong");
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
