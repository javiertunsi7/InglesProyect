package com.englishlearning.domain.enums;

/**
 * Question formats supported by an exercise.
 * TRANSLATION         -> translate an English word/phrase into Spanish.
 * REVERSE_TRANSLATION -> translate a Spanish word/phrase into English.
 * MULTIPLE_CHOICE     -> pick one option among several.
 * FILL_BLANK          -> complete a sentence with the missing word.
 * LISTENING           -> hear audio via TTS, answer a question about it.
 * WORD_ORDER          -> arrange jumbled words into the correct order.
 * MATCHING            -> pair items from two columns.
 * DICTATION           -> hear audio via TTS, type what you hear.
 */
public enum QuestionType {
    TRANSLATION, REVERSE_TRANSLATION, MULTIPLE_CHOICE, FILL_BLANK,
    LISTENING, WORD_ORDER, MATCHING, DICTATION, SPEAKING
}
