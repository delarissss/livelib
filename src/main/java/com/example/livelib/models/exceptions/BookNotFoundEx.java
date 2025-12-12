package com.example.livelib.models.exceptions;

public class BookNotFoundEx extends RuntimeException {
    public BookNotFoundEx(String message) {
        super(message);
    }
    public BookNotFoundEx(String message, Throwable cause) {
        super(message, cause);
    }
}
