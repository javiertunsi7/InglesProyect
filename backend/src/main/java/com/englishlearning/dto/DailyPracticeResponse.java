package com.englishlearning.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Sesión de práctica diaria curada con Spaced Repetition.
 *
 * <p>El cliente la consume y responde cada pregunta con el endpoint existente
 * {@code POST /v1/questions/{questionId}/answer}, que actualiza la
 * planificación SRS de cada pregunta.
 *
 * <ul>
 *   <li>{@code dueCount}: preguntas en la sesión cuya fecha de repaso ya
 *       venció (incluye atrasadas).</li>
 *   <li>{@code newCount}: preguntas nuevas (sin attempt previo).</li>
 *   <li>{@code forecast}: cuántos repasos te tocarán los próximos 7 días, así
 *       el usuario puede ver el ritmo.</li>
 * </ul>
 */
public record DailyPracticeResponse(
        LocalDate date,
        String headline,
        String subhead,
        Integer targetMinutes,
        Integer expectedXp,
        Integer dueCount,
        Integer newCount,
        List<ForecastDay> forecast,
        List<QuestionResponse> questions
) {

    /** Conteo previsto de repasos para una fecha futura. */
    public record ForecastDay(LocalDate date, Long count) {}
}
