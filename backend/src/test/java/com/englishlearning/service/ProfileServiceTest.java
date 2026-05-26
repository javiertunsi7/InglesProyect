package com.englishlearning.service;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.dto.ChangePasswordRequest;
import com.englishlearning.dto.UpdateProfileRequest;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.exception.ResourceNotFoundException;
import com.englishlearning.repository.UserRepository;
import com.englishlearning.repository.UserStreakRepository;
import com.englishlearning.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceTest {

    private UserRepository userRepository;
    private UserStreakRepository streakRepository;
    private PasswordEncoder passwordEncoder;
    private ProfileService service;
    private AuthenticatedUser principal;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        streakRepository = mock(UserStreakRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new ProfileService(userRepository, streakRepository, passwordEncoder);
        principal = new AuthenticatedUser(42L, "demo@english.local", "Demo", Role.USER);
    }

    @Test
    @DisplayName("getProfile devuelve displayName + iniciales + streak agregado")
    void getProfileHappyPath() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(sampleUser()));
        when(streakRepository.findByUserId(42L))
                .thenReturn(Optional.of(UserStreak.builder().userId(42L).currentStreak(14).totalXp(2480).build()));

        var profile = service.getProfile(principal);

        assertEquals("Demo User", profile.displayName());
        assertEquals("DU", profile.initials());
        assertEquals(14, profile.currentStreak());
        assertEquals(2480, profile.totalXp());
    }

    @Test
    @DisplayName("getProfile sin streak: devuelve 0 en current/total")
    void getProfileWithoutStreak() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(sampleUser()));
        when(streakRepository.findByUserId(42L)).thenReturn(Optional.empty());

        var profile = service.getProfile(principal);

        assertEquals(0, profile.currentStreak());
        assertEquals(0, profile.totalXp());
    }

    @Test
    @DisplayName("updateProfile aplica solo los campos no nulos del request")
    void updateProfilePartial() {
        User user = sampleUser();
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(streakRepository.findByUserId(42L)).thenReturn(Optional.empty());

        var req = new UpdateProfileRequest("Nuevo Nombre", null, 20, null);
        var profile = service.updateProfile(principal, req);

        assertEquals("Nuevo Nombre", profile.displayName());
        assertEquals(20, profile.dailyGoalMinutes());
        // bio y dailyGoalXp no se han tocado.
        assertEquals("hola mundo", profile.bio());
        assertEquals(100, profile.dailyGoalXp());
    }

    @Test
    @DisplayName("changePassword rechaza si la contraseña actual es incorrecta")
    void changePasswordRejectsBadCurrent() {
        when(userRepository.findById(42L)).thenReturn(Optional.of(sampleUser()));
        when(passwordEncoder.matches(eq("wrong"), anyString())).thenReturn(false);

        var req = new ChangePasswordRequest("wrong", "nuevoPass123");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.changePassword(principal, req));
        assertEquals("La contraseña actual no es correcta.", ex.getMessage());

        // No debe haber tocado el repo de save.
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword rota el hash cuando la actual es correcta")
    void changePasswordHappyPath() {
        User user = sampleUser();
        when(userRepository.findById(42L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("oldPass"), anyString())).thenReturn(true);
        when(passwordEncoder.encode(eq("nuevoPass"))).thenReturn("new-hash");

        service.changePassword(principal, new ChangePasswordRequest("oldPass", "nuevoPass"));

        verify(passwordEncoder, times(1)).encode("nuevoPass");
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("new-hash", user.getPasswordHash());
    }

    @Test
    @DisplayName("Usuario no encontrado en BD → ResourceNotFoundException")
    void userNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProfile(principal));
    }

    private User sampleUser() {
        return User.builder()
                .id(42L)
                .email("demo@english.local")
                .passwordHash("hashed-pass")
                .displayName("Demo User")
                .role(Role.USER)
                .bio("hola mundo")
                .dailyGoalMinutes(10)
                .dailyGoalXp(100)
                .createdAt(Instant.now())
                .build();
    }
}
