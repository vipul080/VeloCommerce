package com.vipul.ecommerce.exception;

public class UnauthorizedOrderException extends RuntimeException {

    public UnauthorizedOrderException(String message) {
        super(message);
    }
}