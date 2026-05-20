package com.englishlearning.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "user_daily_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_daily_stats_user_date",
                columnNames = {"user_id", "on_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "on_date", nullable = false)
    private LocalDate onDate;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "minutes_practiced", nullable = false)
    private Integer minutesPracticed;

    @Column(name = "exercises_completed", nullable = false)
    private Integer exercisesCompleted;

    @Column(name = "daily_goal_minutes", nullable = false)
    private Integer dailyGoalMinutes;

    @Column(name = "daily_goal_xp", nullable = false)
    private Integer dailyGoalXp;
}
