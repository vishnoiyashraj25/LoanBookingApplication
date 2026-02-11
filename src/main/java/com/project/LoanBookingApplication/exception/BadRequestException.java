package com.project.LoanBookingApplication.exception;

/**
 * Thrown when the request has invalid parameters or payload.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
