package com.bitlab.main_service.exception;

/**
 * Thrown when a request is syntactically valid but cannot be processed. Mapped to HTTP 400.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}