package com.englishlearning.service;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.User;
import com.englishlearning.dto.AuthResponse;
import com.englishlearning.dto.LoginRequest;
import com.englishlearning.dto.RegisterRequest;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final long RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Ya existe un usuario con ese correo electrónico.");
        }
        User user = userRepository.save(User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(Role.USER)
                .createdAt(Instant.now())
                .build());
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas."));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Credenciales inválidas.");
        }
        return buildResponse(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElse(null);
        if (user == null) {
            // No revelar si el email existe o no por seguridad
            return;
        }
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(Instant.now().plusSeconds(RESET_TOKEN_EXPIRY_HOURS * 3600));
        userRepository.save(user);
        mailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new BadRequestException("El enlace de recuperación no es válido o ha expirado."));
        if (user.getResetTokenExpiry() == null || Instant.now().isAfter(user.getResetTokenExpiry())) {
            throw new BadRequestException("El enlace de recuperación ha expirado.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(
                jwtService.issueToken(user),
                jwtService.expirationSeconds(),
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole());
    }
}
