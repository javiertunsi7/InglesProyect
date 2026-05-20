package com.englishlearning.mapper;

import com.englishlearning.domain.enums.QuestionType;
import com.englishlearning.domain.model.Question;
import com.englishlearning.domain.model.QuestionOption;
import com.englishlearning.domain.model.UserQuestionAttempt;
import com.englishlearning.dto.QuestionResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QuestionMapper {

    public QuestionResponse toResponse(Question question,
                                       List<QuestionOption> options,
                                       UserQuestionAttempt attempt) {
        List<QuestionResponse.QuestionOptionResponse> mapped = options == null
                ? List.of()
                : options.stream()
                        .map(o -> new QuestionResponse.QuestionOptionResponse(
                                o.getId(), o.getLabel(), o.getValue(), o.getMatchGroup()))
                        .toList();
        boolean answered = attempt != null && attempt.getAttempts() > 0;
        boolean lastCorrect = attempt != null && Boolean.TRUE.equals(attempt.getCorrect());

        boolean hasOptions = question.getType() == QuestionType.MULTIPLE_CHOICE
                || question.getType() == QuestionType.MATCHING;
        String audioText = (question.getType() == QuestionType.LISTENING
                || question.getType() == QuestionType.DICTATION
                || question.getType() == QuestionType.SPEAKING)
                ? (question.getAudioText() != null ? question.getAudioText() : question.getCorrectAnswer())
                : null;
        List<String> availableWords = question.getType() == QuestionType.WORD_ORDER
                ? shuffleWords(question.getCorrectAnswer())
                : null;
        List<QuestionResponse.MatchPair> matchPairs = question.getType() == QuestionType.MATCHING && options != null
                ? buildMatchPairs(options)
                : null;

        return new QuestionResponse(
                question.getId(),
                question.getPosition(),
                question.getType(),
                question.getPrompt(),
                question.getPromptHighlight(),
                question.getContext(),
                question.getHint(),
                hasOptions ? mapped : List.of(),
                answered,
                lastCorrect,
                audioText,
                availableWords,
                matchPairs
        );
    }

    private List<String> shuffleWords(String correctAnswer) {
        List<String> words = Arrays.asList(correctAnswer.trim().split("\\s+"));
        List<String> shuffled = new ArrayList<>(words);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    private List<QuestionResponse.MatchPair> buildMatchPairs(List<QuestionOption> options) {
        Map<String, List<String>> groups = new LinkedHashMap<>();
        for (QuestionOption opt : options) {
            if (opt.getMatchGroup() == null) continue;
            groups.computeIfAbsent(opt.getMatchGroup(), k -> new ArrayList<>()).add(opt.getValue());
        }
        List<QuestionResponse.MatchPair> pairs = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            List<String> vals = entry.getValue();
            if (vals.size() >= 2) {
                pairs.add(new QuestionResponse.MatchPair(vals.get(0), vals.get(1), entry.getKey()));
            }
        }
        return pairs;
    }
}
