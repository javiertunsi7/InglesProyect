package com.englishlearning.dto;

import com.englishlearning.domain.enums.QuestionType;

import java.util.List;

/**
 * Question payload sent to the client to render an exercise question.
 * The correct answer is never exposed; it is checked server-side and
 * returned only on the answer endpoint.
 */
public record QuestionResponse(
        Long id,
        Integer position,
        QuestionType type,
        String prompt,
        String promptHighlight,
        String context,
        String hint,
        List<QuestionOptionResponse> options,
        boolean answered,
        boolean lastAnswerCorrect,
        String audioText,
        List<String> availableWords,
        List<MatchPair> matchPairs
) {

    public record QuestionOptionResponse(Long id, String label, String value, String matchGroup) {}

    public record MatchPair(String leftText, String rightText, String pairId) {}
}
