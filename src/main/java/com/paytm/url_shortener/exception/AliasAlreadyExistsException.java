package com.paytm.url_shortener.exception;

public class AliasAlreadyExistsException extends RuntimeException {
    public AliasAlreadyExistsException(String message) {
        super(message);
    }
}