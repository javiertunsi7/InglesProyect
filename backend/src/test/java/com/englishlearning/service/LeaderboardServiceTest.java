package com.englishlearning.service;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaderboardServiceTest {

    private UserRepository userRepository;
    private LeaderboardService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        service = new LeaderboardService(userRepository);
    }

    @Test
    @DisplayName("Sin usuarios → entries vacío y currentUser null")
    void emptyLeaderboard() {
        List<Object[]> rows = List.of();
        when(userRepository.findLeaderboard(any())).thenReturn(rows);

        var result = service.getLeaderboard(1L);

        assertEquals(0, result.entries().size());
        assertNull(result.currentUser());
    }

    @Test
    @DisplayName("Asigna rank ascendente desde 1 según el orden del repo")
    void rankingFollowsRepositoryOrder() {
        List<Object[]> rows = List.of(
                row(userOf(1L, "Alice Ada"), streakOf(1L, 5000, 21, 30)),
                row(userOf(2L, "Bob Smith"), streakOf(2L, 3000, 10, 12)),
                row(userOf(3L, "Carl"), streakOf(3L, 1000, 3, 7))
        );
        when(userRepository.findLeaderboard(any())).thenReturn(rows);

        var result = service.getLeaderboard(2L);

        assertEquals(3, result.entries().size());
        assertEquals(1, result.entries().get(0).rank());
        assertEquals("Alice Ada", result.entries().get(0).displayName());
        assertEquals("AA", result.entries().get(0).initials());
        assertEquals(5000, result.entries().get(0).totalXp());
        assertEquals(2, result.entries().get(1).rank());
        assertEquals(3, result.entries().get(2).rank());
    }

    @Test
    @DisplayName("currentUser apunta a la entry del usuario logueado")
    void currentUserMatched() {
        List<Object[]> rows = List.of(
                row(userOf(1L, "Alice"), streakOf(1L, 5000, 21, 30)),
                row(userOf(2L, "Bob"), streakOf(2L, 3000, 10, 12))
        );
        when(userRepository.findLeaderboard(any())).thenReturn(rows);

        var result = service.getLeaderboard(2L);

        assertNotNull(result.currentUser());
        assertEquals(2L, result.currentUser().userId());
        assertEquals(2, result.currentUser().rank());
    }

    @Test
    @DisplayName("Usuario sin streak: se renderiza con 0 en xp y racha")
    void userWithoutStreak() {
        List<Object[]> rows = List.<Object[]>of(row(userOf(1L, "Alone"), null));
        when(userRepository.findLeaderboard(any())).thenReturn(rows);

        var result = service.getLeaderboard(null);

        assertEquals(1, result.entries().size());
        assertEquals(0, result.entries().get(0).totalXp());
        assertEquals(0, result.entries().get(0).currentStreak());
        assertEquals(0, result.entries().get(0).longestStreak());
        assertNull(result.currentUser());
    }

    private Object[] row(User u, UserStreak s) {
        return new Object[]{u, s};
    }

    private User userOf(long id, String name) {
        return User.builder()
                .id(id).email(id + "@test.local").passwordHash("x")
                .displayName(name).role(Role.USER).build();
    }

    private UserStreak streakOf(long userId, int totalXp, int current, int longest) {
        return UserStreak.builder()
                .userId(userId).totalXp(totalXp)
                .currentStreak(current).longestStreak(longest).build();
    }
}
