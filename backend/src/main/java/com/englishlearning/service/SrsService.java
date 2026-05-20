package com.englishlearning.service;

import com.englishlearning.domain.model.UserQuestionAttempt;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

/**
 * Spaced Repetition System (SM-2 simplificado).
 *
 * Cada {@link UserQuestionAttempt} lleva un mini-scheduler: cuántos aciertos
 * seguidos lleva ({@code repetitions}), un factor de facilidad
 * ({@code easeFactor}, 1.3–2.5) que reduce/incrementa la velocidad a la que
 * crece el intervalo, el {@code intervalDays} actual y la próxima fecha de
 * repaso ({@code nextReviewDate}).
 *
 * <p>Al acertar:
 * <ol>
 *   <li>{@code repetitions += 1}.</li>
 *   <li>Si {@code repetitions == 1} → interval = 1 día.</li>
 *   <li>Si {@code repetitions == 2} → interval = 3 días.</li>
 *   <li>Si {@code repetitions >= 3} → interval = round(interval * easeFactor).</li>
 * </ol>
 *
 * <p>Al fallar:
 * <ol>
 *   <li>{@code repetitions = 0}, {@code intervalDays = 1}.</li>
 *   <li>{@code easeFactor = max(MIN_EASE, easeFactor - 0.2)}.</li>
 * </ol>
 *
 * <p>El {@link Clock} es inyectable para que los tests puedan congelar la
 * fecha sin depender del calendario real.
 */
@Service
public class SrsService {

    static final BigDecimal INITIAL_EASE = new BigDecimal("2.50");
    static final BigDecimal MIN_EASE     = new BigDecimal("1.30");
    static final BigDecimal EASE_PENALTY = new BigDecimal("0.20");

    private final Clock clock;

    public SrsService() {
        this(Clock.systemDefaultZone());
    }

    /** Constructor para tests: permite fijar la fecha de "hoy". */
    public SrsService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Aplica el algoritmo SRS al attempt dado y le actualiza los 4 campos.
     * No persiste — el caller es quien guarda. Si el attempt no tiene los
     * campos SRS inicializados (objeto recién creado en memoria), los
     * inicializa antes de aplicar la regla.
     *
     * @param attempt el attempt que el usuario acaba de responder.
     * @param correct true si la respuesta fue correcta.
     */
    public void scheduleNext(UserQuestionAttempt attempt, boolean correct) {
        ensureDefaults(attempt);
        LocalDate today = LocalDate.now(clock);

        if (correct) {
            int repetitions = attempt.getRepetitions() + 1;
            int interval;
            if (repetitions == 1) {
                interval = 1;
            } else if (repetitions == 2) {
                interval = 3;
            } else {
                BigDecimal grown = BigDecimal.valueOf(attempt.getIntervalDays())
                        .multiply(attempt.getEaseFactor())
                        .setScale(0, RoundingMode.HALF_UP);
                interval = Math.max(1, grown.intValue());
            }
            attempt.setRepetitions(repetitions);
            attempt.setIntervalDays(interval);
            attempt.setNextReviewDate(today.plusDays(interval));
            // El ease se mantiene en aciertos; solo se penaliza en fallos.
        } else {
            attempt.setRepetitions(0);
            attempt.setIntervalDays(1);
            attempt.setNextReviewDate(today.plusDays(1));
            BigDecimal newEase = attempt.getEaseFactor().subtract(EASE_PENALTY);
            if (newEase.compareTo(MIN_EASE) < 0) {
                newEase = MIN_EASE;
            }
            attempt.setEaseFactor(newEase);
        }
    }

    /**
     * Garantiza que los campos SRS estén inicializados antes de aplicar la
     * regla. Es relevante la primera vez que un attempt entra al ciclo
     * (cuando se acaba de crear desde {@code ProgressService}).
     */
    private void ensureDefaults(UserQuestionAttempt attempt) {
        if (attempt.getRepetitions() == null) attempt.setRepetitions(0);
        if (attempt.getEaseFactor() == null) attempt.setEaseFactor(INITIAL_EASE);
        if (attempt.getIntervalDays() == null) attempt.setIntervalDays(0);
        if (attempt.getNextReviewDate() == null) {
            attempt.setNextReviewDate(LocalDate.now(clock));
        }
    }

    /** Hoy según el clock inyectado. Útil para que callers fuera del service también lo lean. */
    public LocalDate today() {
        return LocalDate.now(clock);
    }
}
