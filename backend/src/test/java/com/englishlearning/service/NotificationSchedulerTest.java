package com.englishlearning.service;

import com.englishlearning.domain.model.UserDailyStats;
import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del recordatorio diario de racha. La regla es: a las 20:00, mandar
 * push a cada usuario que NO haya practicado hoy. Verificamos que el
 * "practicado hoy" funciona contra UserDailyStatsRepository.
 */
class NotificationSchedulerTest {

    private PushNotificationService pushService;
    private UserRepository userRepository;
    private UserDailyStatsRepository dailyStatsRepository;
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        pushService = mock(PushNotificationService.class);
        userRepository = mock(UserRepository.class);
        dailyStatsRepository = mock(UserDailyStatsRepository.class);
        scheduler = new NotificationScheduler(pushService, userRepository, dailyStatsRepository);
        // @Value en el campo dailyReminderEnabled; en test lo seteamos a true.
        ReflectionTestUtils.setField(scheduler, "dailyReminderEnabled", true);
    }

    @Test
    @DisplayName("Usuario sin stats hoy → recibe push")
    void usersWithoutStatsTodayGetReminder() {
        when(userRepository.findAllIds()).thenReturn(List.of(1L, 2L));
        // Ningún user tiene stats hoy.
        when(dailyStatsRepository.findByUserIdAndOnDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        scheduler.sendStreakReminders();

        verify(pushService, times(1)).sendToUser(eq(1L), any(), any(), any());
        verify(pushService, times(1)).sendToUser(eq(2L), any(), any(), any());
    }

    @Test
    @DisplayName("Usuario que ya practicó hoy → NO recibe push")
    void usersWithStatsTodayAreSkipped() {
        when(userRepository.findAllIds()).thenReturn(List.of(1L));
        UserDailyStats stats = UserDailyStats.builder()
                .userId(1L)
                .onDate(LocalDate.now())
                .xpEarned(50)
                .minutesPracticed(8)
                .build();
        when(dailyStatsRepository.findByUserIdAndOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(stats));

        scheduler.sendStreakReminders();

        verify(pushService, never()).sendToUser(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("dailyReminderEnabled=false → no se envía ningún push aunque haya usuarios sin practicar")
    void disabledFlagShortCircuits() {
        ReflectionTestUtils.setField(scheduler, "dailyReminderEnabled", false);

        scheduler.sendStreakReminders();

        verify(userRepository, never()).findAllIds();
        verify(pushService, never()).sendToUser(anyLong(), any(), any(), any());
    }

    @Test
    @DisplayName("Mezcla: usuario A practicó, B no → solo B recibe push")
    void onlyInactiveUsersGetReminder() {
        when(userRepository.findAllIds()).thenReturn(List.of(1L, 2L));
        // 1L practicó, 2L no.
        when(dailyStatsRepository.findByUserIdAndOnDate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(UserDailyStats.builder().userId(1L).build()));
        when(dailyStatsRepository.findByUserIdAndOnDate(eq(2L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        scheduler.sendStreakReminders();

        verify(pushService, never()).sendToUser(eq(1L), any(), any(), any());
        verify(pushService, times(1)).sendToUser(eq(2L), any(), any(), any());
    }
}
