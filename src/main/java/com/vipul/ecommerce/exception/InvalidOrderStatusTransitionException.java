package com.vipul.ecommerce.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}