package com.englishlearning.service;

import com.englishlearning.domain.enums.CategoryType;
import com.englishlearning.domain.enums.LevelCode;
import com.englishlearning.domain.enums.ProgressStatus;
import com.englishlearning.domain.enums.QuestionType;
import com.englishlearning.domain.model.Category;
import com.englishlearning.domain.model.Exercise;
import com.englishlearning.domain.model.Level;
import com.englishlearning.domain.model.Question;
import com.englishlearning.domain.model.QuestionOption;
import com.englishlearning.domain.model.UserExerciseProgress;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.dto.DailyPracticeResponse;
import com.englishlearning.dto.QuestionResponse;
import com.englishlearning.mapper.QuestionMapper;
import com.englishlearning.repository.CategoryRepository;
import com.englishlearning.repository.ExerciseRepository;
import com.englishlearning.repository.LevelRepository;
import com.englishlearning.repository.QuestionOptionRepository;
import com.englishlearning.repository.QuestionRepository;
import com.englishlearning.repository.UserExerciseProgressRepository;
import com.englishlearning.repository.UserQuestionAttemptRepository;
import com.englishlearning.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Construye la sesión de "Práctica diaria" con un Spaced Repetition System
 * (SRS) estilo SM-2. La sesión combina:
 *
 * <ol>
 *   <li><b>Due</b> ({@link #MAX_DUE}): preguntas cuya {@code next_review_date}
 *       ya llegó. Vienen ordenadas por antigüedad para que las más atrasadas
 *       se vean primero.</li>
 *   <li><b>New</b> ({@link #MAX_NEW}): preguntas que el usuario nunca ha
 *       intentado, extraídas del próximo ejercicio en curso. Es el "tema del
 *       día" para introducir vocabulario nuevo gradualmente.</li>
 *   <li><b>Fallback</b>: si la combinación anterior no alcanza
 *       {@link #MIN_QUESTIONS}, se completa con preguntas del primer
 *       ejercicio (TECH/A1#1) para que la sesión nunca quede vacía.</li>
 * </ol>
 *
 * Los conteos {@code dueCount} y {@code newCount} se devuelven explícitos en
 * el DTO para que la UI pueda mostrar "X repasos · Y palabras nuevas". El
 * forecast de los próximos 7 días viene del repositorio en la misma llamada.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PracticeService {

    private static final int MAX_DUE          = 15;
    private static final int MAX_NEW          = 10;
    private static final int MIN_QUESTIONS    = 6;
    private static final int FORECAST_DAYS    = 7;
    private static final int TARGET_MINUTES   = 10;
    private static final int XP_PER_QUESTION  = 10;

    private final UserExerciseProgressRepository progressRepository;
    private final UserQuestionAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final ExerciseRepository exerciseRepository;
    private final LevelRepository levelRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionMapper questionMapper;
    private final SrsService srsService;

    public DailyPracticeResponse buildDailySession(AuthenticatedUser user) {
        Long userId = user != null ? user.id() : null;
        LocalDate today = srsService.today();

        Set<Long> picked = new LinkedHashSet<>();
        List<Question> ordered = new ArrayList<>();
        int dueCount = 0;
        int newCount = 0;

        if (userId != null) {
            // 1. DUE — preguntas que tocan repasar hoy o estaban atrasadas.
            List<UserQuestionAttempt> due = attemptRepository
                    .findByUserIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(userId, today);
            if (!due.isEmpty()) {
                List<Long> ids = due.stream()
                        .limit(MAX_DUE)
                        .map(UserQuestionAttempt::getQuestionId)
                        .toList();
                List<Question> dueQuestions = questionRepository.findByIdInOrderByPositionAsc(ids);
                // Mantener el orden urgencia (ids) en lugar del orden por posición
                Map<Long, Question> byId = new HashMap<>();
                for (Question q : dueQuestions) byId.put(q.getId(), q);
                for (Long id : ids) {
                    Question q = byId.get(id);
                    if (q != null && picked.add(q.getId())) {
                        ordered.add(q);
                        dueCount++;
                    }
                }
            }

            // 2. NEW — preguntas nuevas del próximo ejercicio en curso.
            int newAdded = addNewFromInProgressExercise(userId, MAX_NEW, picked, ordered);
            newCount += newAdded;
        }

        // 3. Fallback — A1#1 si la sesión es demasiado corta.
        if (ordered.size() < MIN_QUESTIONS) {
            int added = fillFromFallback(MIN_QUESTIONS - ordered.size(), picked, ordered);
            newCount += added;
        }

        List<DailyPracticeResponse.ForecastDay> forecast = buildForecast(userId, today);
        return assemble(user, ordered, dueCount, newCount, forecast, today);
    }

    /**
     * Añade hasta {@code limit} preguntas SIN attempt previo, tomadas del
     * ejercicio en curso más reciente del usuario.
     *
     * @return cuántas se añadieron realmente.
     */
    private int addNewFromInProgressExercise(Long userId, int limit,
                                              Set<Long> picked, List<Question> ordered) {
        List<UserExerciseProgress> recent = progressRepository.findByUserIdOrderByLastSeenAtDesc(userId);
        Optional<UserExerciseProgress> current = recent.stream()
                .filter(p -> p.getStatus() == ProgressStatus.IN_PROGRESS
                          || p.getStatus() == ProgressStatus.AVAILABLE)
                .findFirst();
        if (current.isEmpty()) {
            return 0;
        }
        Long exerciseId = current.get().getExerciseId();
        List<Question> qs = questionRepository.findByExerciseIdOrderByPositionAsc(exerciseId);
        if (qs.isEmpty()) return 0;
        Map<Long, UserQuestionAttempt> attempts = new HashMap<>();
        attemptRepository
                .findByUserIdAndQuestionIdIn(userId, qs.stream().map(Question::getId).toList())
                .forEach(a -> attempts.put(a.getQuestionId(), a));
        int taken = 0;
        for (Question q : qs) {
            if (taken >= limit) break;
            if (attempts.containsKey(q.getId())) continue; // ya en el ciclo SRS
            if (picked.add(q.getId())) {
                ordered.add(q);
                taken++;
            }
        }
        return taken;
    }

    private int fillFromFallback(int needed, Set<Long> picked, List<Question> ordered) {
        Optional<Category> tech = categoryRepository.findByType(CategoryType.TECH);
        if (tech.isEmpty()) return 0;
        Optional<Level> a1 = levelRepository.findByCategoryIdAndCode(tech.get().getId(), LevelCode.A1);
        if (a1.isEmpty()) return 0;
        Optional<Exercise> first = exerciseRepository.findByLevelIdAndPosition(a1.get().getId(), 1);
        if (first.isEmpty()) return 0;
        List<Question> qs = questionRepository.findByExerciseIdOrderByPositionAsc(first.get().getId());
        int taken = 0;
        for (Question q : qs) {
            if (taken >= needed) break;
            if (picked.add(q.getId())) {
                ordered.add(q);
                taken++;
            }
        }
        return taken;
    }

    /** Forecast de cuántas preguntas debes repasar cada día durante los próximos {@link #FORECAST_DAYS}. */
    private List<DailyPracticeResponse.ForecastDay> buildForecast(Long userId, LocalDate today) {
        List<DailyPracticeResponse.ForecastDay> out = new ArrayList<>(FORECAST_DAYS);
        if (userId == null) {
            for (int i = 0; i < FORECAST_DAYS; i++) {
                out.add(new DailyPracticeResponse.ForecastDay(today.plusDays(i), 0L));
            }
            return out;
        }
        LocalDate from = today;
        LocalDate to = today.plusDays(FORECAST_DAYS - 1L);
        Map<LocalDate, Long> counts = new HashMap<>();
        for (UserQuestionAttempt a : attemptRepository.findByUserIdAndNextReviewDateBetween(userId, from, to)) {
            counts.merge(a.getNextReviewDate(), 1L, Long::sum);
        }
        for (int i = 0; i < FORECAST_DAYS; i++) {
            LocalDate day = today.plusDays(i);
            out.add(new DailyPracticeResponse.ForecastDay(day, counts.getOrDefault(day, 0L)));
        }
        return out;
    }

    private DailyPracticeResponse assemble(AuthenticatedUser user,
                                           List<Question> selection,
                                           int dueCount,
                                           int newCount,
                                           List<DailyPracticeResponse.ForecastDay> forecast,
                                           LocalDate today) {
        if (selection.isEmpty()) {
            return new DailyPracticeResponse(
                    today,
                    "Aún no hay práctica disponible",
                    "Empieza un ejercicio para desbloquear la sesión diaria.",
                    0, 0, 0, 0, forecast, List.of());
        }
        Map<Long, List<QuestionOption>> optionsByQuestion = loadOptions(selection);
        Map<Long, UserQuestionAttempt> attemptsByQuestion = loadAttempts(selection, user);
        List<QuestionResponse> responses = selection.stream()
                .sorted(Comparator.comparing(Question::getPosition))
                .map(q -> questionMapper.toResponse(
                        q,
                        optionsByQuestion.getOrDefault(q.getId(), List.of()),
                        attemptsByQuestion.get(q.getId())))
                .toList();
        String headline = composeHeadline(dueCount, newCount);
        String subhead = composeSubhead(dueCount, newCount);
        int expectedXp = responses.size() * XP_PER_QUESTION;
        return new DailyPracticeResponse(
                today,
                headline,
                subhead,
                TARGET_MINUTES,
                expectedXp,
                dueCount,
                newCount,
                forecast,
                responses
        );
    }

    private String composeHeadline(int dueCount, int newCount) {
        if (dueCount == 0 && newCount == 0) return "Práctica diaria.";
        if (dueCount > 0 && newCount > 0) {
            return dueCount + " repasos · " + newCount + (newCount == 1 ? " palabra nueva." : " palabras nuevas.");
        }
        if (dueCount > 0) return dueCount + (dueCount == 1 ? " repaso por hacer." : " repasos por hacer.");
        return newCount + (newCount == 1 ? " palabra nueva." : " palabras nuevas.");
    }

    private String composeSubhead(int dueCount, int newCount) {
        if (dueCount > 0) {
            return "Empieza por los repasos: aciertas y vuelven más tarde; fallas y vuelven mañana.";
        }
        if (newCount > 0) {
            return "Hoy estrenamos vocabulario. Cuando aciertes una palabra entra al ciclo de repaso.";
        }
        return "Vuelve cuando hayas hecho algún ejercicio para empezar tu rutina diaria.";
    }

    private Map<Long, List<QuestionOption>> loadOptions(List<Question> questions) {
        List<Long> ids = questions.stream()
                .filter(q -> q.getType() == QuestionType.MULTIPLE_CHOICE)
                .map(Question::getId)
                .toList();
        if (ids.isEmpty()) return Map.of();
        Map<Long, List<QuestionOption>> map = new HashMap<>();
        for (QuestionOption option : optionRepository
                .findByQuestionIdInOrderByQuestionIdAscPositionAsc(ids)) {
            map.computeIfAbsent(option.getQuestionId(), k -> new ArrayList<>()).add(option);
        }
        return map;
    }

    private Map<Long, UserQuestionAttempt> loadAttempts(List<Question> questions, AuthenticatedUser user) {
        if (user == null || questions.isEmpty()) return Map.of();
        List<Long> ids = questions.stream().map(Question::getId).toList();
        Map<Long, UserQuestionAttempt> map = new HashMap<>();
        for (UserQuestionAttempt a : attemptRepository.findByUserIdAndQuestionIdIn(user.id(), ids)) {
            map.put(a.getQuestionId(), a);
        }
        return map;
    }
}
