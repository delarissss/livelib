package com.example.livelib.models.exceptions;

public class AuthorNotFoundEx extends RuntimeException {
    public AuthorNotFoundEx(String message) {
        super(message);
    }

    public AuthorNotFoundEx(String message, Throwable cause) {
        super(message, cause);
    }
}