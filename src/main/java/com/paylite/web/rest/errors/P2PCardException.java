package com.paylite.web.rest.errors;

public class P2PCardException extends P2PException {

    public P2PCardException(String message) {
        super(message);
    }

    public P2PCardException(String message, Throwable cause) {
        super(message, cause);
    }
}
