package com.example.bankcards.exception;

public class EntityNotFoundException extends RuntimeException {

    private String message;

    public EntityNotFoundException(String message) {super(message);}
}
