package com.musicstore.storelocation.utils.exceptions;

public class DuplicateAddressException extends RuntimeException {

    public DuplicateAddressException() {}

    public DuplicateAddressException(String message) {
        super(message);
    }

    public DuplicateAddressException(Throwable cause) {
        super(cause);
    }

    public DuplicateAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}