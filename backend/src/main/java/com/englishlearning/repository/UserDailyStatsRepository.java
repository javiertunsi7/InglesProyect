package com.englishlearning.repository;

import com.englishlearning.domain.model.UserDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDailyStatsRepository extends JpaRepository<UserDailyStats, Long> {

    Optional<UserDailyStats> findByUserIdAndOnDate(Long userId, LocalDate onDate);

    List<UserDailyStats> findByUserIdAndOnDateBetweenOrderByOnDateAsc(
            Long userId, LocalDate from, LocalDate to);
}
