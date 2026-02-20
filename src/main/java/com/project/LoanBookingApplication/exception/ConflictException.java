package com.project.LoanBookingApplication.exception;

/**
 * Thrown when an operation conflicts with the current state (e.g. duplicate, already processed).
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

