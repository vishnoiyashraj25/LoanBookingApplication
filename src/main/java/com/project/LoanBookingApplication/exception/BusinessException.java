package com.project.LoanBookingApplication.exception;

/**
 * Thrown for business rule failures (e.g. already processed, invalid state).
 * Consumer treats this as "reject and ack" — no retry, no DLT.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
