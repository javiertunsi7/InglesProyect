package com.englishlearning.repository;

import com.englishlearning.domain.model.WordOfDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WordOfDayRepository extends JpaRepository<WordOfDay, Long> {

    Optional<WordOfDay> findByOnDate(LocalDate date);

    Optional<WordOfDay> findFirstByOrderByOnDateDesc();
}
