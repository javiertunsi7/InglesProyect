package com.englishlearning.service;

import com.englishlearning.repository.UserDailyStatsRepository;
import com.englishlearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final PushNotificationService pushService;
    private final UserRepository userRepository;
    private final UserDailyStatsRepository dailyStatsRepository;

    @Value("${app.notifications.daily-reminder.enabled:true}")
    private boolean dailyReminderEnabled;

    @Scheduled(cron = "0 0 20 * * ?")
    public void sendStreakReminders() {
        if (!dailyReminderEnabled) {
            return;
        }

        LocalDate today = LocalDate.now();
        List<Long> allUserIds = userRepository.findAllIds();
        int sent = 0;

        for (Long userId : allUserIds) {
            boolean practicedToday = dailyStatsRepository.findByUserIdAndOnDate(userId, today).isPresent();
            if (!practicedToday) {
                pushService.sendToUser(userId,
                        "¡No olvides practicar hoy!",
                        "Mantén tu racha activa. Dedica unos minutos a tu inglés.",
                        "/practice");
                sent++;
            }
        }

        if (sent > 0) {
            log.info("Sent {} streak reminder push notifications", sent);
        }
    }
}
