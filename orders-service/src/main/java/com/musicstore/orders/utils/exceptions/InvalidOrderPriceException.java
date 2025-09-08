package com.musicstore.orders.utils.exceptions;

public class InvalidOrderPriceException extends RuntimeException {

    public InvalidOrderPriceException() {}

    public InvalidOrderPriceException(String message) { super(message); }

    public InvalidOrderPriceException(Throwable cause) { super(cause); }

    public InvalidOrderPriceException(String message, Throwable cause) { super(message, cause); }
}
