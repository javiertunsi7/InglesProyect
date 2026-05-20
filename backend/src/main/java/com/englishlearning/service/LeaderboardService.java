package com.englishlearning.service;

import com.englishlearning.domain.model.User;
import com.englishlearning.domain.model.UserStreak;
import com.englishlearning.dto.LeaderboardEntry;
import com.englishlearning.dto.LeaderboardResponse;
import com.englishlearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaderboardService {

    private static final int LEADERBOARD_LIMIT = 100;

    private final UserRepository userRepository;

    public LeaderboardResponse getLeaderboard(Long currentUserId) {
        List<Object[]> rows = userRepository.findLeaderboard(PageRequest.of(0, LEADERBOARD_LIMIT));
        List<LeaderboardEntry> entries = new ArrayList<>();
        LeaderboardEntry currentUserEntry = null;

        int rank = 0;
        for (Object[] row : rows) {
            rank++;
            User user = (User) row[0];
            UserStreak streak = (UserStreak) row[1];
            LeaderboardEntry entry = new LeaderboardEntry(
                    rank,
                    user.getId(),
                    user.getDisplayName(),
                    initials(user.getDisplayName()),
                    streak != null ? streak.getTotalXp() : 0,
                    streak != null ? streak.getCurrentStreak() : 0,
                    streak != null ? streak.getLongestStreak() : 0
            );
            entries.add(entry);
            if (currentUserId != null && user.getId().equals(currentUserId)) {
                currentUserEntry = entry;
            }
        }

        return new LeaderboardResponse(entries, currentUserEntry);
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