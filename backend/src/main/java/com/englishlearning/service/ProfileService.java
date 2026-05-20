package com.englishlearning.service;

import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.dto.ChangePasswordRequest;
import com.englishlearning.dto.UpdateProfileRequest;
import com.englishlearning.dto.UserProfileResponse;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserStreakRepository;
import com.englishlearning.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final UserStreakRepository streakRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(AuthenticatedUser principal) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + principal.id()));
        UserStreak streak = streakRepository.findByUserId(user.getId()).orElse(null);
        return toProfileResponse(user, streak);
    }

    public UserProfileResponse updateProfile(AuthenticatedUser principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + principal.id()));

        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.setDisplayName(request.displayName());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        if (request.dailyGoalMinutes() != null) {
            user.setDailyGoalMinutes(request.dailyGoalMinutes());
        }
        if (request.dailyGoalXp() != null) {
            user.setDailyGoalXp(request.dailyGoalXp());
        }
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        UserStreak streak = streakRepository.findByUserId(user.getId()).orElse(null);
        return toProfileResponse(user, streak);
    }

    public void changePassword(AuthenticatedUser principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + principal.id()));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual no es correcta.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private UserProfileResponse toProfileResponse(User user, UserStreak streak) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                initials(user.getDisplayName()),
                user.getBio(),
                user.getAvatarUrl(),
                user.getRole(),
                streak != null ? streak.getCurrentStreak() : 0,
                streak != null ? streak.getTotalXp() : 0,
                user.getDailyGoalMinutes(),
                user.getDailyGoalXp(),
                user.getCreatedAt()
        );
    }

    private String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length && sb.length() < 2; i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }
}
