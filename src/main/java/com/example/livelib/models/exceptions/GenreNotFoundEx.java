package com.example.livelib.models.exceptions;

public class GenreNotFoundEx extends RuntimeException {
    public GenreNotFoundEx(String message) {
        super(message);
    }
    public GenreNotFoundEx(String message, Throwable cause) {
        super(message, cause);
    }
}
