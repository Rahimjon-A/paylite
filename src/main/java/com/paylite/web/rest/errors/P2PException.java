package com.paylite.web.rest.errors;

public class P2PException extends RuntimeException {

    public P2PException(String message) {
        super(message);
    }

    public P2PException(String message, Throwable cause) {
        super(message, cause);
    }
}
