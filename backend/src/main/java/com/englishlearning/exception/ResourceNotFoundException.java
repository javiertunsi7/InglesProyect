package com.englishlearning.exception;

/**
 * Thrown when a requested entity does not exist.
 * Caught by GlobalExceptionHandler and translated into HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
