package com.englishlearning.controller;

import com.englishlearning.dto.AuthResponse;
import com.englishlearning.dto.LoginRequest;
import com.englishlearning.dto.RegisterRequest;
import com.englishlearning.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        authService.forgotPassword(email);
        return ResponseEntity.ok(Map.of("message", "Si el correo existe, recibirás un enlace para restablecer tu contraseña."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.getOrDefault("token", "").trim();
        String newPassword = body.getOrDefault("newPassword", "").trim();
        if (token.isEmpty() || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token inválido o contraseña demasiado corta."));
        }
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
    }
}
