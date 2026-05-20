package com.englishlearning.service;

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
import com.englishlearning.dto.AnswerRequest;
import com.englishlearning.dto.AnswerResponse;
import com.englishlearning.dto.ExerciseDetailResponse;
import com.englishlearning.dto.QuestionResponse;
import com.englishlearning.exception.BadRequestException;
import com.englishlearning.exception.ForbiddenException;
import com.englishlearning.exception.ResourceNotFoundException;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final CategoryRepository categoryRepository;
    private final LevelRepository levelRepository;
    private final ExerciseRepository exerciseRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserQuestionAttemptRepository attemptRepository;
    private final QuestionMapper questionMapper;
    private final ProgressService progressService;
    private final SubscriptionService subscriptionService;

    public ExerciseDetailResponse findDetail(Long exerciseId, AuthenticatedUser user) {
        Exercise exercise = loadExercise(exerciseId);
        Level level = levelRepository.findById(exercise.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el nivel " + exercise.getLevelId()));
        requirePremiumAccess(level, user);
        Category category = categoryRepository.findById(level.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la categoría " + level.getCategoryId()));

        List<Question> questions = questionRepository.findByExerciseIdOrderByPositionAsc(exerciseId);
        Map<Long, List<QuestionOption>> optionsByQuestion = loadOptions(questions);
        Map<Long, UserQuestionAttempt> attemptsByQuestion = loadAttempts(questions, user);
        List<QuestionResponse> responses = new ArrayList<>();
        for (Question q : questions) {
            responses.add(questionMapper.toResponse(
                    q,
                    optionsByQuestion.getOrDefault(q.getId(), List.of()),
                    attemptsByQuestion.get(q.getId())));
        }
        UserExerciseProgress progress = user == null
                ? null
                : progressRepository.findByUserIdAndExerciseId(user.id(), exerciseId).orElse(null);
        ProgressStatus status = progress != null
                ? progress.getStatus()
                : (Boolean.TRUE.equals(exercise.getLocked()) ? ProgressStatus.LOCKED : ProgressStatus.AVAILABLE);
        int correct = progress != null ? progress.getCorrectAnswers() : 0;
        int total = progress != null ? progress.getTotalAnswers() : 0;
        int stars = progress != null ? progress.getStars() : 0;
        return new ExerciseDetailResponse(
                exercise.getId(),
                category.getType(),
                level.getCode(),
                level.getDisplayName(),
                exercise.getPosition(),
                exercise.getTitle(),
                exercise.getTopic(),
                exercise.getQuestionsCount(),
                exercise.getEstimatedMinutes(),
                exercise.getXpReward(),
                status,
                stars,
                correct,
                total,
                responses
        );
    }

    @Transactional
    public AnswerResponse submitAnswer(Long questionId, AnswerRequest request, AuthenticatedUser user) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la pregunta con id " + questionId));
        Exercise exercise = exerciseRepository.findById(question.getExerciseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el ejercicio con id " + question.getExerciseId()));
        Level level = levelRepository.findById(exercise.getLevelId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el nivel " + exercise.getLevelId()));
        requirePremiumAccess(level, user);

        String input = request.answer();
        if (input == null || input.isBlank()) {
            throw new BadRequestException("La respuesta no puede estar vacía.");
        }
        boolean correct = isCorrect(question, input);
        if (user == null) {
            String message = correct ? "¡Correcto!" : "Incorrecto, sigue intentándolo.";
            return new AnswerResponse(correct, message,
                    correct ? question.getCorrectAnswer() : null,
                    question.getExplanation(),
                    ProgressStatus.AVAILABLE, false, 0, 0, 0, 0);
        }
        ProgressService.AttemptOutcome outcome = progressService
                .recordQuestionAttempt(user.id(), exercise, question, correct);
        UserExerciseProgress progress = outcome.progress();
        String message = correct
                ? (outcome.justCompleted() ? "¡Ejercicio completado!" : "¡Correcto!")
                : "Incorrecto, sigue intentándolo.";
        return new AnswerResponse(
                correct,
                message,
                correct ? question.getCorrectAnswer() : null,
                question.getExplanation(),
                progress.getStatus(),
                outcome.justCompleted(),
                outcome.xpAwarded(),
                progress.getCorrectAnswers(),
                progress.getTotalAnswers(),
                progress.getStars()
        );
    }

    private Map<Long, List<QuestionOption>> loadOptions(List<Question> questions) {
        List<Long> ids = questions.stream()
                .filter(q -> q.getType() == QuestionType.MULTIPLE_CHOICE
                        || q.getType() == QuestionType.MATCHING)
                .map(Question::getId)
                .toList();
        if (ids.isEmpty()) return Map.of();
        Map<Long, List<QuestionOption>> map = new HashMap<>();
        for (QuestionOption option : optionRepository
                .findByQuestionIdInOrderByQuestionIdAscPositionAsc(ids)) {
            map.computeIfAbsent(option.getQuestionId(), key -> new ArrayList<>()).add(option);
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

    private boolean isCorrect(Question question, String input) {
        return switch (question.getType()) {
            case MULTIPLE_CHOICE ->
                question.getCorrectAnswer().trim().equalsIgnoreCase(input.trim());
            case WORD_ORDER -> {
                String normalizedInput = input.trim().replaceAll("\\s+", " ").toLowerCase();
                String normalizedAnswer = question.getCorrectAnswer().trim().replaceAll("\\s+", " ").toLowerCase();
                yield normalizedInput.equals(normalizedAnswer);
            }
            case MATCHING -> isMatchingCorrect(question, input);
            default ->
                normalize(question.getCorrectAnswer()).equals(normalize(input));
        };
    }

    private boolean isMatchingCorrect(Question question, String input) {
        // Answer format: "groupId:optionId,groupId:optionId,..."
        // Each groupId should match the matchGroup of the paired option.
        List<QuestionOption> allOptions = optionRepository.findByQuestionIdOrderByPositionAsc(question.getId());
        Map<Long, String> optionGroupMap = new HashMap<>();
        for (QuestionOption opt : allOptions) {
            if (opt.getMatchGroup() != null) {
                optionGroupMap.put(opt.getId(), opt.getMatchGroup());
            }
        }
        String[] pairs = input.trim().split(",");
        for (String pair : pairs) {
            String[] parts = pair.trim().split(":");
            if (parts.length != 2) return false;
            String expectedGroup = parts[0].trim();
            try {
                Long optionId = Long.parseLong(parts[1].trim());
                String actualGroup = optionGroupMap.get(optionId);
                if (!expectedGroup.equals(actualGroup)) return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private String normalize(String value) {
        String trimmed = value.trim().toLowerCase();
        String stripped = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return stripped.replaceAll("[\\p{Punct}]", "").replaceAll("\\s+", " ");
    }

    private void requirePremiumAccess(Level level, AuthenticatedUser user) {
        if (level.getCode() == LevelCode.C1 || level.getCode() == LevelCode.C2) {
            if (user == null || !subscriptionService.isPremium(user.id())) {
                throw new ForbiddenException("Este nivel requiere una suscripción Premium.");
            }
        }
    }

    private Exercise loadExercise(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el ejercicio con id " + exerciseId));
    }
}
