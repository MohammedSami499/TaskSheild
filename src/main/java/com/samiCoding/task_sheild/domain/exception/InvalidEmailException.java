package com.samiCoding.task_sheild.domain.exception;

/**
 * Thrown when email validation fails
 */
public class InvalidEmailException extends DomainException {

    public InvalidEmailException(String email) {
        super(String.format("Invalid email address: %s", email));
    }

    public InvalidEmailException(String email, Throwable cause) {
        super(String.format("Invalid email address: %s", email), cause);
    }
}
