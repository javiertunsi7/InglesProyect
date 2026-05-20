package com.englishlearning.service;

import com.englishlearning.domain.model.UserQuestionAttempt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests para el algoritmo SRS (SM-2 simplificado).
 *
 * <p>El servicio es lógica pura: se inyecta un {@link Clock#fixed} para que
 * "hoy" sea siempre {@code 2026-05-15} y los aserts sobre fechas no dependan
 * del calendario real.
 */
class SrsServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 15);

    private SrsService srs;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-05-15T12:00:00Z"), ZoneOffset.UTC);
        srs = new SrsService(fixedClock);
    }

    @Test
    @DisplayName("Primer acierto: repetitions 0→1, interval 1, próximo = hoy+1")
    void firstCorrectAnswer() {
        UserQuestionAttempt attempt = freshAttempt();

        srs.scheduleNext(attempt, true);

        assertEquals(1, attempt.getRepetitions());
        assertEquals(1, attempt.getIntervalDays());
        assertEquals(TODAY.plusDays(1), attempt.getNextReviewDate());
        // El ease no se penaliza en acierto.
        assertEquals(0, new BigDecimal("2.50").compareTo(attempt.getEaseFactor()));
    }

    @Test
    @DisplayName("Segundo acierto: repetitions 1→2, interval 3, próximo = hoy+3")
    void secondCorrectAnswer() {
        UserQuestionAttempt attempt = freshAttempt();
        attempt.setRepetitions(1);
        attempt.setIntervalDays(1);

        srs.scheduleNext(attempt, true);

        assertEquals(2, attempt.getRepetitions());
        assertEquals(3, attempt.getIntervalDays());
        assertEquals(TODAY.plusDays(3), attempt.getNextReviewDate());
    }

    @Test
    @DisplayName("Tercer acierto: interval = round(3 * 2.50) = 8, próximo = hoy+8")
    void thirdCorrectAnswerUsesEaseFactor() {
        UserQuestionAttempt attempt = freshAttempt();
        attempt.setRepetitions(2);
        attempt.setIntervalDays(3);
        attempt.setEaseFactor(new BigDecimal("2.50"));

        srs.scheduleNext(attempt, true);

        assertEquals(3, attempt.getRepetitions());
        // 3 * 2.50 = 7.50 → HALF_UP → 8
        assertEquals(8, attempt.getIntervalDays());
        assertEquals(TODAY.plusDays(8), attempt.getNextReviewDate());
    }

    @Test
    @DisplayName("Acierto repetido con ease bajo: cuarto acierto desde ease=1.40, interval=8")
    void correctAnswerWithLowEase() {
        UserQuestionAttempt attempt = freshAttempt();
        attempt.setRepetitions(3);
        attempt.setIntervalDays(8);
        attempt.setEaseFactor(new BigDecimal("1.40"));

        srs.scheduleNext(attempt, true);

        assertEquals(4, attempt.getRepetitions());
        // 8 * 1.40 = 11.20 → HALF_UP → 11
        assertEquals(11, attempt.getIntervalDays());
    }

    @Test
    @DisplayName("Fallo tras 5 aciertos: reset repetitions, interval=1, ease baja 0.20")
    void failureResetsRepetitionsAndPenalizesEase() {
        UserQuestionAttempt attempt = freshAttempt();
        attempt.setRepetitions(5);
        attempt.setIntervalDays(15);
        attempt.setEaseFactor(new BigDecimal("2.50"));

        srs.scheduleNext(attempt, false);

        assertEquals(0, attempt.getRepetitions());
        assertEquals(1, attempt.getIntervalDays());
        assertEquals(TODAY.plusDays(1), attempt.getNextReviewDate());
        assertEquals(0, new BigDecimal("2.30").compareTo(attempt.getEaseFactor()));
    }

    @Test
    @DisplayName("El ease nunca baja de 1.30 aunque se penalice varias veces")
    void easeNeverGoesBelowFloor() {
        UserQuestionAttempt attempt = freshAttempt();
        attempt.setEaseFactor(new BigDecimal("1.40"));

        srs.scheduleNext(attempt, false);   // 1.40 − 0.20 = 1.20 → MIN 1.30
        assertEquals(0, new BigDecimal("1.30").compareTo(attempt.getEaseFactor()));

        srs.scheduleNext(attempt, false);   // 1.30 − 0.20 = 1.10 → MIN 1.30
        assertEquals(0, new BigDecimal("1.30").compareTo(attempt.getEaseFactor()));
    }

    @Test
    @DisplayName("ensureDefaults: un attempt sin campos SRS se inicializa al primer scheduleNext")
    void schedulingInitializesNullFields() {
        UserQuestionAttempt attempt = UserQuestionAttempt.builder()
                .userId(1L)
                .questionId(10L)
                .attempts(0)
                .correct(false)
                .hintsUsed(0)
                .lastSeenAt(Instant.now())
                // Sin repetitions, easeFactor, intervalDays, nextReviewDate.
                .build();

        srs.scheduleNext(attempt, true);

        assertNotNull(attempt.getRepetitions());
        assertNotNull(attempt.getEaseFactor());
        assertNotNull(attempt.getIntervalDays());
        assertNotNull(attempt.getNextReviewDate());
        // El primer acierto sobre defaults debe comportarse como C1.
        assertEquals(1, attempt.getRepetitions());
        assertEquals(1, attempt.getIntervalDays());
        assertEquals(TODAY.plusDays(1), attempt.getNextReviewDate());
    }

    @Test
    @DisplayName("today() devuelve la fecha del Clock inyectado, no el calendario del sistema")
    void todayUsesInjectedClock() {
        assertEquals(TODAY, srs.today());
    }

    /**
     * Construye un attempt con los campos SRS ya inicializados a sus valores
     * por defecto (lo que haría {@code ProgressService} al crear uno nuevo).
     */
    private UserQuestionAttempt freshAttempt() {
        return UserQuestionAttempt.builder()
                .userId(1L)
                .questionId(10L)
                .attempts(0)
                .correct(false)
                .hintsUsed(0)
                .lastSeenAt(Instant.now())
                .repetitions(0)
                .easeFactor(new BigDecimal("2.50"))
                .intervalDays(0)
                .nextReviewDate(TODAY)
                .build();
    }
}
