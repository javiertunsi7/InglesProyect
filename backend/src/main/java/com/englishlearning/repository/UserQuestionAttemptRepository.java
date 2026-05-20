package com.englishlearning.repository;

import com.englishlearning.domain.model.UserQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestionAttemptRepository extends JpaRepository<UserQuestionAttempt, Long> {

    Optional<UserQuestionAttempt> findByUserIdAndQuestionId(Long userId, Long questionId);

    List<UserQuestionAttempt> findByUserIdAndQuestionIdIn(Long userId, Collection<Long> questionIds);

    List<UserQuestionAttempt> findByUserIdAndCorrectFalseOrderByLastSeenAtDesc(Long userId);

    /* === SRS: scheduling queries === */

    /** Preguntas debidas (con fecha de repaso <= corte) para este usuario, ordenadas por urgencia. */
    List<UserQuestionAttempt> findByUserIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(
            Long userId, LocalDate cutoff);

    /** Cuántas preguntas debe repasar el usuario antes (o el mismo día) de la fecha de corte. */
    long countByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate cutoff);

    /** Preguntas con repaso programado entre dos fechas — usado para construir el forecast. */
    List<UserQuestionAttempt> findByUserIdAndNextReviewDateBetween(
            Long userId, LocalDate from, LocalDate to);

    /** Todas las preguntas intentadas por un usuario. */
    List<UserQuestionAttempt> findByUserId(Long userId);
}
