package com.musicstore.customers.utils.exceptions;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {}

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(Throwable cause) {
        super(cause);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
