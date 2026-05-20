package com.englishlearning.exception;

import java.time.Instant;

/**
 * Standard error body returned by GlobalExceptionHandler.
 * All messages are in Spanish so the frontend can show them directly.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {}
